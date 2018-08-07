ip4s-cats: Cats interop module
=======================================

# Import

In order to bring typeclass instances and additional functionality into scope, use the following imports

```tut
import com.comcast.ip4s._

// Additional functionality
import com.comcast.ip4s.interop.cats._
// Typeclass instances
import com.comcast.ip4s.interop.cats.implicits._
```

# Hostnames resolution

On the JVM, hostnames can be resolved to IP addresses via `resolve` and `resolveAll`:

```tut
import cats.effect.IO

val home = host"localhost"
val homeIp = HostnameResolver.resolve[IO](home)
homeIp.unsafeRunSync

val homeIps = HostnameResolver.resolveAll[IO](home)
homeIps.unsafeRunSync
```

# Typeclass instances

This modules comes with typeclass instances for many classes in `ip4s`

```tut
import cats._
import cats.implicits._

implicitly[Eq[Hostname]]
implicitly[Order[Hostname]]

val host1 = host"localhost"
val host2 = host"localhost"

host1 === host2

host1 < host2
```
