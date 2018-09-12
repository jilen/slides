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

  case class Packet(bytes: Array[Byte])

  def decodePacket[F[_]](src: Stream[F, Byte]) = {

    @annotation.tailrec
    def splitPackets(packets: Vector[Packet], c: Chunk[Byte]): (Vector[Packet], Chunk[Byte]) = {
      if(c.size < 4) {
        (packets, c)
      } else {
        val lenBytes = c.take(4).toBytes
        val len = ((0xFF & lenBytes(0)) << 24) | ((0xFF & lenBytes(1)) << 16) |
        ((0xFF & lenBytes(2)) << 8) | (0xFF & lenBytes(3));
        if(c.size < 4 + len) {
          (packets, c)
        } else {
          val packetBytes = c.drop(4).take(len).toBytes.toArray
          splitPackets(
            packets :+ Packet(packetBytes),
            c.drop(4 + len)
          )
        }
      }
    }

    def loop(prev: Chunk[Byte], s: Stream[F, Byte]): Pull[F, Packet, Unit] = {
      s.pull.unconsChunk.flatMap {
        case Some((hd, tl)) =>
          val concated = Chunk.concat(Seq(prev, hd))
          val (packets, remain) = splitPackets(Vector.empty, concated)
          Pull.output(Chunk.vector(packets)) >> loop(remain, tl)
        case None =>
          Pull.done
      }
    }
  }
}
