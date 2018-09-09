import cats.effect._
import fs2._

case class User(id: Long)
case class Order(id: Long, userId: Long)

class S[F[_]: Concurrent] {

  def readFrom(minId: Long): F[Seq[User]] = ???
  def sendMsg(u: User): F[Unit] = ???

  def stream() = {
    def loop(from: Long): Stream[F, User] = Stream.eval(readFrom(from)).flatMap {
      case us if !us.isEmpty => Stream.emits(us) ++ loop(us.map(_.id).max)
      case us => Stream.empty
    }
    loop(0)
  }
  stream().evalMap(sendMsg)
  stream().mapAsync(100)(sendMsg)

  def merge[F[_]: ConcurrentEffect, A] {
    def fromQuery: Stream[F, A] = ???
    def fromRealtime: Stream[F, A] = ???
    def stream = fromQuery.merge(fromRealtime)
  }

  def parJoin[F[_]: ConcurrentEffect, A] = {
    def users: Stream[F, User] = ???
    def orders(uid: Long): Stream[F, Order] = ???
    users.map(u => orders(u.id)).parJoin(100)
  }
}
