import cats.effect._
import cats.syntax.all._
import com.github.shyiko.mysql.binlog._
import com.github.shyiko.mysql.binlog.event._
import fs2._
import fs2.concurrent.Queue

object BinLogClient {
  def stream[F[_]](cli: BinaryLogClient)(implicit F: ConcurrentEffect[F]) = {

    def register(queue: Queue[F, Event]) = F.delay {
      cli.registerEventListener(new BinaryLogClient.EventListener() {
        override def onEvent(event: Event) {
          F.toIO(queue.enqueue1(event)).unsafeRunSync()
        }
      })
      cli.connect(3000)
    }

    Stream.bracket {
      Queue.bounded[F, Event](1000).flatTap(register)
    } {
      _ => F.delay(cli.disconnect())
    }.flatMap(q => q.dequeueAvailable)

  }
}
