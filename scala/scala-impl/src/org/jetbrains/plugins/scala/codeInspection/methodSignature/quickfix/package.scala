package org.jetbrains.plugins.scala
package codeInspection
package methodSignature

import java.util.regex.Pattern

import com.intellij.psi.tree.IElementType
import com.intellij.psi.{PsiElement, PsiMethod, PsiType}
import org.jetbrains.plugins.scala.extensions.PsiElementExt
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiElement
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScBlockExpr
import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScFunction, ScFunctionDefinition}
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory
import org.jetbrains.plugins.scala.project.ProjectContext

package object quickfix {

  import ScalaPsiElementFactory.{createBlockFromExpr, createDeclaration}
  import ScalaTokenTypes.{tASSIGN, tCOLON}

  private[this] val MutatorNamePattern = Pattern.compile(
    """(?-i)(?:do|set|add|remove|insert|delete|aquire|release|update)(?:\p{Lu}.*)"""
  )

  def removeTypeElement(function: ScFunction): Unit = for {
    colon <- findChild(function, ScalaTokenTypes.tCOLON)
    typeElement <- function.returnTypeElement
  } function.deleteChildRange(colon, typeElement)

  def removeAssignment(definition: ScFunctionDefinition)
                      (implicit context: ProjectContext): Unit = {
    for {
      expression <- definition.body
      if !expression.isInstanceOf[ScBlockExpr]
      block = createBlockFromExpr(expression)
    } expression.replace(block)

    for {
      assignment <- definition.assignment
    } assignment.delete()
  }

  def addUnitTypeElement(definition: ScFunctionDefinition)
                        (implicit context: ProjectContext): Unit = {
    val declaration = createDeclaration("x", "Unit", isVariable = false, null)
    for {
      colon <- findChild(declaration)
      assignment <- findChild(declaration, tASSIGN)

      expression <- definition.body
    } definition.addRangeAfter(colon, assignment, expression.getPrevSiblingNotWhitespace)
  }

  private[methodSignature] def hasMutatorLikeName(method: PsiMethod): Boolean =
    MutatorNamePattern.matcher(method.getName).matches()

  private[methodSignature] def isMutator: PsiMethod => Boolean = {
    case _: ScalaPsiElement => false // do nothing
    case method => method.getReturnType == PsiType.VOID || hasMutatorLikeName(method)
  }

  private[this] def findChild(element: PsiElement,
                              elementType: IElementType = tCOLON) =
    element.children.find(_.getNode.getElementType == elementType)
}
