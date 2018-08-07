ip4s-scalaz: Scalaz interop module
=======================================

# Import

In order to bring typeclass instances and additional functionality into scope, use the following imports

```scala
scala> import com.comcast.ip4s._
import com.comcast.ip4s._

scala> // Additional functionality
     | import com.comcast.ip4s.interop.scalaz._
import com.comcast.ip4s.interop.scalaz._

scala> // Typeclass instances
     | import com.comcast.ip4s.interop.scalaz.implicits._
import com.comcast.ip4s.interop.scalaz.implicits._
```

# Typeclass instances

This modules comes with typeclass instances for many classes in `ip4s`

```scala
scala> import scalaz._
import scalaz._

scala> import Scalaz._
import Scalaz._

scala> implicitly[Equal[Hostname]]
res2: scalaz.Equal[com.comcast.ip4s.Hostname] = scalaz.Order$$anon$8@3d5ad95c

scala> implicitly[Order[Hostname]]
res3: scalaz.Order[com.comcast.ip4s.Hostname] = scalaz.Order$$anon$8@3d5ad95c

scala> val host1 = host"localhost"
host1: com.comcast.ip4s.Hostname = localhost

scala> val host2 = host"localhost"
host2: com.comcast.ip4s.Hostname = localhost

scala> host1 === host2
res4: Boolean = true

scala> host1 < host2
res5: Boolean = false
```
