import cats.effect._
import cats.syntax.all._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Main {

  def parallel = {
    val f1 = Future {
      Thread.sleep(1000)
      1
    }
    val f2 = Future {
      Thread.sleep(1000)
      2
    }

    for {
      r1 <- f1
      r2 <- f2
    } yield (r1, r2)
  }

  def nonParallel = {
    for {
      r1 <- Future {
        Thread.sleep(1000)
        1
      }
      r2 <- Future {
        Thread.sleep(1000)
        2
      }
    } yield (r1, r2)
  }
}


object IOMain extends IOApp {

  def run(args: List[String]) = {
    ioCancelable.as(ExitCode(0))
  }

  def ioParallel = {
    //Through cats.Parallel
    val f1 = IO.sleep(1.seconds).as(1)
    val f2 = IO.sleep(1.seconds).as(1)
    (f1, f2).parTupled
  }

  def ioCancelable = {

    def setInterval[A](i: FiniteDuration, f: IO[A]): IO[Unit] = {
      def loop(): IO[Unit] = {
        IO.sleep(i) >> f.runAsync(_ => IO.unit).to[IO] >> loop()
      }
      loop().start.flatMap(_.cancel)
    }

    for {
      m <- setInterval(1.seconds, IO(println("Hi"))).start
      _ <- IO.sleep(1.seconds)
      _ <- m.cancel
    } yield {}
}

}
