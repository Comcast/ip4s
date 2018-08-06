ip4s-scalaz: Scalaz interop module
=======================================

# Import

In order to bring typeclass instances and additional functionality into scope, use the following imports

```tut
import com.comcast.ip4s._

// Additional functionality
import com.comcast.ip4s.interop.scalaz._
// Typeclass instances
import com.comcast.ip4s.interop.scalaz.implicits._
```

# Typeclass instances

This modules comes with typeclass instances for many classes in `ip4s`

```tut
import scalaz._
import Scalaz._

implicitly[Equal[Hostname]]
implicitly[Order[Hostname]]

val host1 = host"localhost"
val host2 = host"localhost"

host1 === host2

host1 < host2
```
