package org.jetbrains.plugins.scala
package codeInspection
package methodSignature

import com.intellij.codeInspection._
import com.intellij.openapi.project.Project
import com.intellij.psi.{PsiElement, PsiMethod}
import org.jetbrains.plugins.scala.extensions._
import org.jetbrains.plugins.scala.lang.psi.api.expr.{ScMethodCall, ScReferenceExpression}
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory
import org.jetbrains.plugins.scala.lang.psi.types._
import org.jetbrains.plugins.scala.lang.resolve.ScalaResolveResult
import org.jetbrains.plugins.scala.lang.resolve.processor.CollectMethodsProcessor

/**
  * Pavel Fatin
  */
final class JavaAccessorEmptyParenCallInspection extends AbstractInspection {

  override def actionFor(implicit holder: ProblemsHolder): PartialFunction[PsiElement, Unit] = PartialFunction.empty

  override def buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) =
    new PureFunctionVisitor(holder, isOnTheFly)

  override protected def problemDescriptor(element: PsiElement,
                                           maybeQuickFix: Option[LocalQuickFix],
                                           descriptionTemplate: String,
                                           highlightType: ProblemHighlightType)
                                          (implicit manager: InspectionManager,
                                           isOnTheFly: Boolean): Option[ProblemDescriptor] =
    element match {
      case (reference: ScReferenceExpression) childOf (call: ScMethodCall) if call.argumentExpressions.isEmpty =>
        import JavaAccessorEmptyParenCallInspection._
        val problemExists =
          reference match {
            case _ if call.getParent.isInstanceOf[ScMethodCall] => false
            case Accessor(scType) if processType(scType, reference).size > 1 => hasSameType(call, reference)
            case _ => false
          }

        if (problemExists) super.problemDescriptor(reference.nameId, createQuickFix(call), descriptionTemplate, highlightType)
        else None
      case _ => None
    }
}

object JavaAccessorEmptyParenCallInspection {

  private object Accessor {

    def unapply(reference: ScReferenceExpression): Option[ScType] = reference match {
      case Resolved(resolveResult@ScalaResolveResult(method: PsiMethod, _)) if quickfix.isAccessor(method) => resolveResult.fromType
      case _ => None
    }
  }

  private def processType(`type`: ScType, place: ScReferenceExpression) = {
    val processor = new CollectMethodsProcessor(place, place.refName)
    processor.processType(`type`, place)
    processor.candidatesS
  }

  private def hasSameType(call: ScMethodCall, reference: ScReferenceExpression) = (call, reference) match {
    case (result.Typeable(left), result.Typeable(right)) => left.equiv(right)
    case _ => false
  }

  private def createQuickFix(call: ScMethodCall) = {
    val quickFix = new AbstractFixOnPsiElement("Remove call parentheses",
      call
    ) {
      override protected def doApplyFix(call: ScMethodCall)(implicit project: Project): Unit = {
        val text = call.getInvokedExpr.getText
        call.replace(ScalaPsiElementFactory.createExpressionFromText(text))
      }
    }

    Some(quickFix)
  }
}
