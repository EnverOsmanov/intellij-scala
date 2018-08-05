package org.jetbrains.plugins.scala.failed.typeInference

import org.jetbrains.plugins.scala.PerfCycleTests
import org.jetbrains.plugins.scala.base.ScalaLightCodeInsightFixtureTestAdapter
import org.junit.experimental.categories.Category

/**
  * @author anton.yalyshev
  * @since 14.04.16.
  */
@Category(Array(classOf[PerfCycleTests]))
class FunctionParametersTest extends ScalaLightCodeInsightFixtureTestAdapter {

  override protected def shouldPass: Boolean = false

  def testSCL12708(): Unit = {
    checkTextHasNoErrors(
      s"""
         |private def testFoo(bNum: Byte, sNum: Short, iNum: Int): Unit = { }
         |
         |testFoo(0xa, 0x2a, 0x2a)
      """.stripMargin)
  }
}
