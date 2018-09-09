
import cats.syntax.all._
import cats.effect._
import cats.effect.concurrent._
import scala.concurrent.duration._

final case class State[F[_], A](
  queue: Vector[A],
  deq:   Vector[Deferred[F, A]]
)
class Pool[F[_], A](ref: Ref[F, State[F, A]])
  (implicit F: ConcurrentEffect[F], T: Timer[F]) {

  def enqueue(a: A): F[Unit] = {
    ref.modify { s =>
      if (s.deq.isEmpty) {
        (s.copy(queue = s.queue :+ a), None)
      } else {
        (s.copy(deq = s.deq.tail), Some(s.deq.head))
      }
    }.flatMap {
      case Some(h) =>
        F.runAsync(h.complete(a))(_ => IO.unit).to[F]
      case None =>
        F.unit
    }
  }

  def timedDequeue(duration: FiniteDuration, timer: Timer[F]): F[Option[A]] = {
    cancellableDequeue1().flatMap {
      case (Right(v), _) => F.pure(Some(v))
      case (Left(defer), cancel) =>
        val timeout = T.sleep(duration)
        F.race(timeout, defer.get).flatMap {
          case Right(v) => F.pure(Some(v))
          case Left(_)  => cancel.as(None)
        }
    }

  }

  private def cancellableDequeue1(): F[(Either[Deferred[F, A], A], F[Unit])] = {
    Deferred[F, A].flatMap { defer =>
      ref.modify { s =>
        if (s.queue.isEmpty)
          (s.copy(deq = s.deq :+ defer), None)
        else
          (s.copy(queue = s.queue.drop(1)), Some(s.queue.take(1).head))
      }.map {
        case Some(h) =>
          (Right(h), F.unit)
        case None =>
          (Left(defer), ref.modify { s =>
            (s.copy(deq = s.deq.filterNot(_ == defer)), {})
          })
      }
    }
  }

  private def fork[C](fa: F[C]) = {

  }
}
