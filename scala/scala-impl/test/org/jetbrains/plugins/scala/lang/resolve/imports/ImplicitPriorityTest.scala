package org.jetbrains.plugins.scala
package lang
package resolve
package imports

import org.jetbrains.plugins.scala.lang.psi.api.base.ScReferenceElement

/**
 * @author jzaugg
 */
class ImplicitPriorityTest extends ScalaResolveTestCase {
  override def folderPath(): String = super.folderPath() + "resolve/implicitPriority/"

  def testlowPriorityImplicits() {
    findReferenceAtCaret() match {
      case r: ScReferenceElement =>
        val results = r.multiResolveScala(false)
        assert(results.length == 1, results.mkString(","))
      case x => throw new Exception("Wrong reference!" + x)
    }
  }

  def testlowPriorityImplicits2() {
    findReferenceAtCaret() match {
      case r: ScReferenceElement =>
        val results = r.multiResolveScala(false)
        assert(results.length == 1, results.mkString(","))
      case x => throw new Exception("Wrong reference!" + x)
    }
  }

  def testmostSpecificImplicit() {
    findReferenceAtCaret() match {
      case r: ScReferenceElement =>
        val results = r.multiResolveScala(false)
        assert(results.length == 1, results.mkString(","))
      case x => throw new Exception("Wrong reference!" + x)
    }
  }
}