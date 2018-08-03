package org.jetbrains.plugins.scala
package codeInspection
package methodSignature

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.{PsiElement, PsiMethod}
import org.jetbrains.plugins.scala.extensions._
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiElement
import org.jetbrains.plugins.scala.lang.psi.api.expr.{ScMethodCall, ScReferenceExpression}
import org.jetbrains.plugins.scala.lang.psi.types.ScTypeExt
import org.jetbrains.plugins.scala.lang.resolve.processor.CollectMethodsProcessor

/**
 * Pavel Fatin
 */
final class JavaAccessorMethodCalledAsEmptyParenInspection extends AbstractInspection("Java accessor method called as empty-paren") {

  override def actionFor(implicit holder: ProblemsHolder): PartialFunction[PsiElement, Unit] = {
    case e: ScReferenceExpression => e.getParent match {
      case call: ScMethodCall =>
        call.getParent match {
          case _: ScMethodCall => // do nothing
          case _ => if (call.argumentExpressions.isEmpty) {
            e.resolve() match {
              case _: ScalaPsiElement => // do nothing
              case m: PsiMethod if m.isAccessor && !isOverloadedMethod(e) && hasSameType(call, e) =>
                holder.registerProblem(e.nameId, getDisplayName, new quickfix.RemoveCallParentheses(call))
              case _ =>
            }
          }
        }
      case _ =>
    }
  }

  private def isOverloadedMethod(ref: ScReferenceExpression) = {
    val processor = new CollectMethodsProcessor(ref, ref.refName)
    ref.bind().flatMap(_.fromType).forall(processor.processType(_, ref))
    processor.candidatesS.size > 1
  }

  private def hasSameType(call: ScMethodCall, ref: ScReferenceExpression) = {

    val callType = call.`type`().toOption
    val refType = ref.`type`().toOption
    (callType, refType) match {
      case (Some(t1), Some(t2)) => t1.equiv(t2)
      case _ => false
    }
  }
}
