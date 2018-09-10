
import cats._
import cats.free._
import cats.syntax.all._


trait UserAlg[F[_]] {
  def add(u: User): F[Long]
  def get(id: Long): F[Option[User]]
}

class AlgWithF[F[_]](alg: UserAlg[F])(implicit F: Monad[F]) {
  def init(user: User) = alg.get(user.id).flatMap {
    case None => alg.add(user).map(id => user.copy(id = id))
    case Some(h) => F.pure(h)
  }
}



class AdtWithFree {

  sealed trait UserOpA[A]
  case class Add(u: User) extends UserOpA[Long]
  case class Get(id: Long) extends UserOpA[Option[User]]

  type UserOp[A] = Free[UserOpA, A]

  def add(u: User): UserOp[Long] = Free.liftF[UserOpA, Long](new Add(u))
  def get(id: Long): UserOp[Option[User]] = Free.liftF[UserOpA, Option[User]](new Get(id))

  def init(u: User) = {
    get(u.id).flatMap {
      case Some(u) => Free.pure(u)
      case None => add(u).map(id => u.copy(id = id))
    }
  }

}
