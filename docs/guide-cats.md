ip4s-cats: Cats interop module
=======================================

# Import

In order to bring typeclass instances and additional functionality into scope, use the following imports

```scala
scala> import com.comcast.ip4s._
import com.comcast.ip4s._

scala> // Additional functionality
     | import com.comcast.ip4s.interop.cats._
import com.comcast.ip4s.interop.cats._

scala> // Typeclass instances
     | import com.comcast.ip4s.interop.cats.implicits._
import com.comcast.ip4s.interop.cats.implicits._
```

# Hostnames resolution

On the JVM, hostnames can be resolved to IP addresses via `resolve` and `resolveAll`:

```scala
scala> import cats.effect.IO
import cats.effect.IO

scala> val home = host"localhost"
home: com.comcast.ip4s.Hostname = localhost

scala> val homeIp = HostnameResolver.resolve[IO](home)
homeIp: cats.effect.IO[Option[com.comcast.ip4s.IpAddress]] = IO$80191396

scala> homeIp.unsafeRunSync
res2: Option[com.comcast.ip4s.IpAddress] = Some(127.0.0.1)

scala> val homeIps = HostnameResolver.resolveAll[IO](home)
homeIps: cats.effect.IO[Option[cats.data.NonEmptyList[com.comcast.ip4s.IpAddress]]] = IO$1478873688

scala> homeIps.unsafeRunSync
res3: Option[cats.data.NonEmptyList[com.comcast.ip4s.IpAddress]] = Some(NonEmptyList(127.0.0.1, ::1))
```

# Typeclass instances

This modules comes with typeclass instances for many classes in `ip4s`

```scala
scala> import cats._
import cats._

scala> import cats.implicits._
import cats.implicits._

scala> implicitly[Eq[Hostname]]
res4: cats.Eq[com.comcast.ip4s.Hostname] = cats.kernel.Order$$anon$117@336e3e9e

scala> implicitly[Order[Hostname]]
res5: cats.Order[com.comcast.ip4s.Hostname] = cats.kernel.Order$$anon$117@336e3e9e

scala> val host1 = host"localhost"
host1: com.comcast.ip4s.Hostname = localhost

scala> val host2 = host"localhost"
host2: com.comcast.ip4s.Hostname = localhost

scala> host1 === host2
res6: Boolean = true

scala> host1 < host2
res7: Boolean = false
```
