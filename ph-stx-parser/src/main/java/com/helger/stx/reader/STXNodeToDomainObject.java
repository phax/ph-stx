/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.stx.reader;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.ext.CommonsArrayList;
import com.helger.commons.collection.ext.ICommonsList;
import com.helger.stx.model.ESTXOperator;
import com.helger.stx.model.ESTXPathOperator;
import com.helger.stx.model.ESTXQuantifiedExpressionType;
import com.helger.stx.model.ISTXExpression;
import com.helger.stx.model.ISTXLiteralExpression;
import com.helger.stx.model.ISTXObject;
import com.helger.stx.model.ISTXPrimaryExpression;
import com.helger.stx.model.ISTXStepExpression;
import com.helger.stx.model.STXBinaryExpression;
import com.helger.stx.model.STXContextItemExpression;
import com.helger.stx.model.STXExpressionList;
import com.helger.stx.model.STXFilterExpression;
import com.helger.stx.model.STXForExpression;
import com.helger.stx.model.STXFunctionCall;
import com.helger.stx.model.STXIfExpression;
import com.helger.stx.model.STXNCName;
import com.helger.stx.model.STXNumericLiteral;
import com.helger.stx.model.STXParenthesizedExpression;
import com.helger.stx.model.STXPath;
import com.helger.stx.model.STXPathExpression;
import com.helger.stx.model.STXPredicate;
import com.helger.stx.model.STXPredicateList;
import com.helger.stx.model.STXQuantifiedExpression;
import com.helger.stx.model.STXRelativePathExpression;
import com.helger.stx.model.STXStringLiteral;
import com.helger.stx.model.STXUnaryExpression;
import com.helger.stx.model.STXVarNameAndExpression;
import com.helger.stx.model.STXVariableReference;
import com.helger.stx.model.axisstep.IXP2SingleStep;
import com.helger.stx.model.axisstep.XP2AbbreviatedAttributeStep;
import com.helger.stx.model.axisstep.XP2AbbreviatedElementStep;
import com.helger.stx.model.axisstep.XP2AbbreviatedReverseStep;
import com.helger.stx.model.axisstep.XP2AxisStep;
import com.helger.stx.model.nodetest.ISTXKindTest;
import com.helger.stx.model.nodetest.ISTXNameTest;
import com.helger.stx.model.nodetest.ISTXNodeTest;
import com.helger.stx.model.nodetest.STXCDataTest;
import com.helger.stx.model.nodetest.STXCommentTest;
import com.helger.stx.model.nodetest.STXDocTypeTest;
import com.helger.stx.model.nodetest.STXLocalNameIsWildcardTest;
import com.helger.stx.model.nodetest.STXNamespaceIsWildcardTest;
import com.helger.stx.model.nodetest.STXNodeTest;
import com.helger.stx.model.nodetest.STXProcessingInstructionTest;
import com.helger.stx.model.nodetest.STXQNameTest;
import com.helger.stx.model.nodetest.STXTextTest;
import com.helger.stx.model.nodetest.STXWildcardTest;
import com.helger.stx.parser.ParserQName;
import com.helger.stx.parser.ParserSTXTreeConstants;
import com.helger.stx.parser.STXNode;

/**
 * This class is responsible for converting the abstract syntax tree created by
 * JavaCC to a domain model.
 *
 * @author Philip Helger
 */
public final class STXNodeToDomainObject
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (STXNodeToDomainObject.class);

  private STXNodeToDomainObject ()
  {}

  private static void _expectNodeType (@Nonnull final STXNode aNode, final int nExpected)
  {
    if (aNode.getNodeType () != nExpected)
      throw new STXHandlingException (aNode,
                                      "Expected a '" +
                                             ParserSTXTreeConstants.jjtNodeName[nExpected] +
                                             "' node but received a '" +
                                             ParserSTXTreeConstants.jjtNodeName[aNode.getNodeType ()] +
                                             "'");
  }

  private static void _throwUnexpectedChildrenCount (@Nonnull final STXNode aNode, @Nonnull @Nonempty final String sMsg)
  {
    s_aLogger.error (sMsg + " (having " + aNode.jjtGetNumChildren () + " children)");
    for (int i = 0; i < aNode.jjtGetNumChildren (); ++i)
      s_aLogger.error ("  " + aNode.jjtGetChild (i));
    throw new STXHandlingException (aNode, sMsg);
  }

  // [59] PITest ::= "processing-instruction" "(" (NCName | StringLiteral)? ")"
  @Nonnull
  private static STXProcessingInstructionTest _convertProcessingInstructionTest (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTPITEST);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount > 1)
      _throwUnexpectedChildrenCount (aNode, "Expected at last 1 child!");

    if (nChildCount == 1)
    {
      final STXNode aChildNode = aNode.jjtGetChild (0);
      _expectNodeType (aChildNode, ParserSTXTreeConstants.JJTPISTRINGLITERAL);
      return new STXProcessingInstructionTest (new STXStringLiteral (aChildNode.getText ()));
    }

    final String sText = aNode.getText ();
    if (sText == null)
      return new STXProcessingInstructionTest ();
    return new STXProcessingInstructionTest (new STXNCName (sText));
  }

  // [58] CommentTest ::= "comment" "(" ")"
  @Nonnull
  private static STXCommentTest _convertCommentTest (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTCOMMENTTEST);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 0)
      _throwUnexpectedChildrenCount (aNode, "Expected no child!");

    return new STXCommentTest ();
  }

  // [57] TextTest ::= "text" "(" ")"
  @Nonnull
  private static STXTextTest _convertTextTest (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTTEXTTEST);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 0)
      _throwUnexpectedChildrenCount (aNode, "Expected no child!");

    return new STXTextTest ();
  }

  // [55] AnyKindTest ::= "node" "(" ")"
  @Nonnull
  private static STXNodeTest _convertAnyKindTest (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTANYKINDTEST);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 0)
      _throwUnexpectedChildrenCount (aNode, "Expected no child!");

    return new STXNodeTest ();
  }

  // [78a] CdataTest ::= "cdata" "(" ")"
  @Nonnull
  private static STXCDataTest _convertCDataTest (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTCDATATEST);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 0)
      _throwUnexpectedChildrenCount (aNode, "Expected no child!");

    return new STXCDataTest ();
  }

  // [78b] DoctypeTest ::= "doctype" "(" ")"
  @Nonnull
  private static STXDocTypeTest _convertDocTypeTest (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTDOCTYPETEST);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 0)
      _throwUnexpectedChildrenCount (aNode, "Expected no child!");

    return new STXDocTypeTest ();
  }

  // [54] KindTest ::= DocumentTest| ElementTest| AttributeTest|
  // SchemaElementTest| SchemaAttributeTest| PITest| CommentTest| TextTest|
  // AnyKindTest
  @Nonnull
  private static ISTXKindTest _convertKindTest (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTKINDTEST);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 1)
      _throwUnexpectedChildrenCount (aNode, "Expected 1 child!");

    final STXNode aChildNode = aNode.jjtGetChild (0);
    switch (aChildNode.getNodeType ())
    {
      case ParserSTXTreeConstants.JJTPITEST:
        return _convertProcessingInstructionTest (aChildNode);
      case ParserSTXTreeConstants.JJTCOMMENTTEST:
        return _convertCommentTest (aChildNode);
      case ParserSTXTreeConstants.JJTTEXTTEST:
        return _convertTextTest (aChildNode);
      case ParserSTXTreeConstants.JJTANYKINDTEST:
        return _convertAnyKindTest (aChildNode);
      case ParserSTXTreeConstants.JJTCDATATEST:
        return _convertCDataTest (aChildNode);
      case ParserSTXTreeConstants.JJTDOCTYPETEST:
        return _convertDocTypeTest (aChildNode);
      // TODO cdata and doctype
      default:
        throw new STXHandlingException (aChildNode,
                                        "Invalid node type for kind test: " +
                                                    ParserSTXTreeConstants.jjtNodeName[aChildNode.getNodeType ()]);
    }
  }

  // [48] FunctionCall ::= QName "(" (ExprSingle ("," ExprSingle)*)? ")"
  @Nonnull
  private static STXFunctionCall _convertFunctionCall (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTFUNCTIONCALL);
    final int nChildCount = aNode.jjtGetNumChildren ();

    final ParserQName aFunctionName = (ParserQName) aNode.getValue ();

    final ICommonsList <ISTXExpression> aExpressions = new CommonsArrayList<> ();
    for (int i = 0; i < nChildCount; ++i)
    {
      final STXNode aChildNode = aNode.jjtGetChild (i);
      aExpressions.add (_convertExpressionSingle (aChildNode));
    }

    return new STXFunctionCall (aFunctionName, aExpressions);
  }

  // [47] ContextItemExpr ::= "."
  @Nonnull
  private static STXContextItemExpression _convertContextItemExpression (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTCONTEXTITEMEXPR);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 0)
      _throwUnexpectedChildrenCount (aNode, "Expected no 0 child!");

    return new STXContextItemExpression ();
  }

  // [46] ParenthesizedExpr ::= "(" Expr? ")"
  @Nonnull
  private static STXParenthesizedExpression _convertParenthesizedExpression (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTPARENTHESIZEDEXPR);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount > 1)
      _throwUnexpectedChildrenCount (aNode, "Expected at last 1 child!");

    STXExpressionList aExprList = null;
    if (nChildCount == 1)
      aExprList = _convertExpressionList (aNode.jjtGetChild (0));

    return new STXParenthesizedExpression (aExprList);
  }

  // [45] VarName ::= QName
  @Nonnull
  private static ParserQName _convertVarName (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTVARNAME);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 0)
      _throwUnexpectedChildrenCount (aNode, "Expected exactly 0 children!");

    return (ParserQName) aNode.getValue ();
  }

  // [44] VarRef ::= "$" VarName
  @Nonnull
  private static STXVariableReference _convertVarRef (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTVARREF);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 1)
      _throwUnexpectedChildrenCount (aNode, "Expected exactly 1 child!");

    final ParserQName aVarName = _convertVarName (aNode.jjtGetChild (0));
    return new STXVariableReference (aVarName);
  }

  // [43] NumericLiteral ::= IntegerLiteral | DecimalLiteral | DoubleLiteral
  // [42] Literal ::= NumericLiteral | StringLiteral
  @Nonnull
  private static ISTXLiteralExpression _convertLiteral (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTLITERAL);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 0)
      _throwUnexpectedChildrenCount (aNode, "Expected no child!");

    final Object aValue = aNode.getValue ();
    if (aValue instanceof BigInteger)
      return new STXNumericLiteral ((BigInteger) aValue);
    if (aValue instanceof BigDecimal)
      return new STXNumericLiteral ((BigDecimal) aValue);
    if (aValue instanceof String)
      return new STXStringLiteral ((String) aValue);
    throw new STXHandlingException (aNode, "Invalid node value type: " + aValue.getClass ());
  }

  // [41] PrimaryExpr ::= Literal | VarRef | ParenthesizedExpr | ContextItemExpr
  // | FunctionCall
  @Nonnull
  private static ISTXPrimaryExpression _convertPrimaryExpression (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTPRIMARYEXPR);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 1)
      _throwUnexpectedChildrenCount (aNode, "Expected exactly 1 child!");

    final STXNode aChildNode = aNode.jjtGetChild (0);
    switch (aChildNode.getNodeType ())
    {
      case ParserSTXTreeConstants.JJTLITERAL:
        return _convertLiteral (aChildNode);
      case ParserSTXTreeConstants.JJTVARREF:
        return _convertVarRef (aChildNode);
      case ParserSTXTreeConstants.JJTPARENTHESIZEDEXPR:
        return _convertParenthesizedExpression (aChildNode);
      case ParserSTXTreeConstants.JJTCONTEXTITEMEXPR:
        return _convertContextItemExpression (aChildNode);
      case ParserSTXTreeConstants.JJTFUNCTIONCALL:
        return _convertFunctionCall (aChildNode);
      default:
        throw new STXHandlingException (aChildNode, "Invalid node type for primary expression!");
    }
  }

  // [40] Predicate ::= "[" Expr "]"
  @Nonnull
  private static STXPredicate _convertPredicate (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTPREDICATE);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 1)
      _throwUnexpectedChildrenCount (aNode, "Expected exactly 1 child!");

    final STXExpressionList aExpressionList = _convertExpressionList (aNode.jjtGetChild (0));
    return new STXPredicate (aExpressionList);
  }

  // [38] FilterStep ::= PrimaryExpr Predicate*
  @Nonnull
  private static STXFilterExpression _convertFilterStep (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTFILTERSTEP);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount < 1)
      _throwUnexpectedChildrenCount (aNode, "Expected at least 1 children!");

    final ISTXPrimaryExpression aExpr = _convertPrimaryExpression (aNode.jjtGetChild (0));

    final ICommonsList <STXPredicate> aPredicates = new CommonsArrayList<> ();
    for (int i = 1; i < nChildCount; ++i)
    {
      final STXPredicate aPredicate = _convertPredicate (aNode.jjtGetChild (i));
      aPredicates.add (aPredicate);
    }
    final STXPredicateList aPredicateList = new STXPredicateList (aPredicates);
    return new STXFilterExpression (aExpr, aPredicateList);
  }

  // [37] Wildcard ::= "*" | (NCName ":" "*") | ("*" ":" NCName)
  @Nonnull
  private static ISTXNameTest _convertWildcard (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTWILDCARD);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 0)
      _throwUnexpectedChildrenCount (aNode, "Expected exactly 0 children!");

    if (aNode.getValue () == null)
    {
      // Only "*"
      return new STXWildcardTest ();
    }

    final boolean bNamespaceIsWildcard = ((Boolean) aNode.getValue ()).booleanValue ();
    if (bNamespaceIsWildcard)
      return new STXNamespaceIsWildcardTest (aNode.getText ());

    // else local name is wildcard
    return new STXLocalNameIsWildcardTest (aNode.getText ());
  }

  // [36] NameTest ::= QName | Wildcard
  @Nonnull
  private static ISTXNameTest _convertNameTest (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTNAMETEST);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount > 1)
      _throwUnexpectedChildrenCount (aNode, "Expected at last 1 child!");

    if (nChildCount == 0)
    {
      final ParserQName aQName = (ParserQName) aNode.getValue ();
      return new STXQNameTest (aQName);
    }

    // Must be a wildcard
    return _convertWildcard (aNode.jjtGetChild (0));
  }

  // [35] NodeTest ::= KindTest | NameTest
  @Nonnull
  private static ISTXNodeTest _convertNodeTest (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTNODETEST);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 1)
      _throwUnexpectedChildrenCount (aNode, "Expected exactly 1 child!");

    final STXNode aChildNode = aNode.jjtGetChild (0);
    switch (aChildNode.getNodeType ())
    {
      case ParserSTXTreeConstants.JJTKINDTEST:
        return _convertKindTest (aChildNode);
      case ParserSTXTreeConstants.JJTNAMETEST:
        return _convertNameTest (aChildNode);
      default:
        throw new STXHandlingException (aChildNode, "Invalid node type for node test!");
    }
  }

  // [34] AbbrevReverseStep ::= ".."
  @Nonnull
  private static XP2AbbreviatedReverseStep _convertAbbreviatedReverseStep (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTABBREVREVERSESTEP);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 0)
      _throwUnexpectedChildrenCount (aNode, "Expected exactly 0 children!");

    return new XP2AbbreviatedReverseStep ();
  }

  // [32] ReverseStep ::= (ReverseAxis NodeTest) | AbbrevReverseStep
  @Nonnull
  private static IXP2SingleStep _convertReverseStep (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTREVERSESTEP);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 1)
      _throwUnexpectedChildrenCount (aNode, "Expected 1 children!");

    return _convertAbbreviatedReverseStep (aNode.jjtGetChild (0));
  }

  // [31] AbbrevForwardStep ::= "@"? NodeTest
  private static IXP2SingleStep _convertAbbreviatedForwardStep (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTABBREVFORWARDSTEP);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 1 && nChildCount != 2)
      _throwUnexpectedChildrenCount (aNode, "Expected 1 or 2 children!");

    if (nChildCount == 1)
    {
      final ISTXNodeTest aNodeTest = _convertNodeTest (aNode.jjtGetChild (0));
      return new XP2AbbreviatedElementStep (aNodeTest);
    }

    // child "0" is the "@" sign
    final ISTXNodeTest aNodeTest = _convertNodeTest (aNode.jjtGetChild (1));
    return new XP2AbbreviatedAttributeStep (aNodeTest);
  }

  // [29] ForwardStep ::= AbbrevForwardStep
  @Nonnull
  private static IXP2SingleStep _convertForwardStep (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTFORWARDSTEP);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 1)
      _throwUnexpectedChildrenCount (aNode, "Expected 1 children!");

    return _convertAbbreviatedForwardStep (aNode.jjtGetChild (0));
  }

  // [28] AxisStep ::= (ReverseStep | ForwardStep) Predicate?
  @Nonnull
  private static XP2AxisStep _convertAxisStep (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTAXISSTEP);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount < 1 || nChildCount > 2)
      _throwUnexpectedChildrenCount (aNode, "Expected exactly 1 or 2 children!");

    final STXNode aChildNode = aNode.jjtGetChild (0);
    IXP2SingleStep aSingleStep;
    if (aChildNode.getNodeType () == ParserSTXTreeConstants.JJTREVERSESTEP)
      aSingleStep = _convertReverseStep (aChildNode);
    else
      aSingleStep = _convertForwardStep (aChildNode);

    final ICommonsList <STXPredicate> aPredicates = new CommonsArrayList<> ();
    if (nChildCount > 1)
    {
      final STXPredicate aPredicate = _convertPredicate (aNode.jjtGetChild (1));
      aPredicates.add (aPredicate);
    }
    return new XP2AxisStep (aSingleStep, new STXPredicateList (aPredicates));
  }

  // [27] StepExpr ::= AxisStep
  @Nonnull
  private static ISTXStepExpression _convertStepExpression (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTSTEPEXPR);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 1)
      _throwUnexpectedChildrenCount (aNode, "Expected exactly 1 child!");

    final STXNode aChildNode = aNode.jjtGetChild (0);
    return _convertAxisStep (aChildNode);
  }

  // [26] RelativePathExpr ::= StepExpr (("/" | "//") StepExpr)*
  @Nonnull
  private static ISTXExpression _convertRelativePathExpression (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTRELATIVEPATHEXPR);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount < 1 || (nChildCount % 2) != 1)
      _throwUnexpectedChildrenCount (aNode, "Expected 1 or an odd number of children!");

    if (nChildCount == 1)
    {
      // no relative path expression - take only the contained step expression
      return _convertStepExpression (aNode.jjtGetChild (0));
    }

    // Maintain the order and make no prefix/postfix differentiation
    final ICommonsList <ISTXObject> aElements = new CommonsArrayList<> ();
    for (int i = 0; i < nChildCount; ++i)
    {
      final STXNode aChildNode = aNode.jjtGetChild (i);
      if ((i % 2) == 0)
        aElements.add (_convertStepExpression (aChildNode));
      else
        aElements.add (ESTXPathOperator.getFromIDOrThrow (aChildNode.getText ()));
    }
    return new STXRelativePathExpression (aElements);
  }

  // [25] PathExpr ::= ("/" RelativePathExpr?) | ("//" RelativePathExpr) |
  // RelativePathExpr
  @Nonnull
  private static ISTXExpression _convertPathExpression (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTPATHEXPR);
    final int nChildCount = aNode.jjtGetNumChildren ();

    final String sOperator = aNode.getText ();
    if (sOperator == null)
    {
      // No operator present - use only relative path
      return _convertRelativePathExpression (aNode.jjtGetChild (0));
    }

    final ESTXPathOperator eOperator = ESTXPathOperator.getFromIDOrThrow (sOperator);
    final ISTXExpression aExpr = nChildCount == 0 ? null : _convertRelativePathExpression (aNode.jjtGetChild (0));
    return new STXPathExpression (eOperator, aExpr);
  }

  // [21] ValueExpr ::= PathExpr | FilterStep
  @Nonnull
  private static ISTXExpression _convertValueExpression (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTVALUEEXPR);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 1)
      _throwUnexpectedChildrenCount (aNode, "Expected exactly 1 child!");

    final STXNode aChildNode = aNode.jjtGetChild (0);
    if (aChildNode.getNodeType () == ParserSTXTreeConstants.JJTPATHEXPR)
      return _convertPathExpression (aChildNode);
    return _convertFilterStep (aChildNode);
  }

  // [20] UnaryExpr ::= ("-" | "+")* UnionExpr
  @Nonnull
  private static ISTXExpression _convertUnaryExpression (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTUNARYEXPR);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount < 1)
      _throwUnexpectedChildrenCount (aNode, "Expected at least 1 child!");

    if (nChildCount == 1)
    {
      // no unary expression - take only the contained value expression
      return _convertUnionExpression (aNode.jjtGetChild (0));
    }

    // Multiple operators are allowed
    ESTXOperator eFinalOperator = null;
    for (int i = 0; i < nChildCount - 1; ++i)
    {
      final ESTXOperator eOperator = ESTXOperator.getFromIDOrThrow (aNode.jjtGetChild (i).getText ());
      if (eFinalOperator == null)
        eFinalOperator = eOperator;
      else
        if (eOperator != eFinalOperator)
        {
          // ("+" and "-") or ("-" and "+") -> "-"
          eFinalOperator = ESTXOperator.MINUS;
        }
    }
    final ISTXExpression aExpr = _convertUnionExpression (aNode.jjtGetChild (nChildCount - 1));
    final STXUnaryExpression ret = new STXUnaryExpression (eFinalOperator, aExpr);
    return ret;
  }

  // [19] CastExpr ::= ComparisonExpr
  @Nonnull
  private static ISTXExpression _convertCastExpression (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTCASTEXPR);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 1)
      _throwUnexpectedChildrenCount (aNode, "Expected 1 children!");

    return _convertComparisonExpression (aNode.jjtGetChild (0));
  }

  // [18] CastableExpr ::= CastExpr
  @Nonnull
  private static ISTXExpression _convertCastableAsExpression (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTCASTABLEEXPR);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 1)
      _throwUnexpectedChildrenCount (aNode, "Expected 1 children!");

    // no castable expression - take only the contained cast expression
    return _convertCastExpression (aNode.jjtGetChild (0));
  }

  // [17] TreatExpr ::= CastableExpr
  @Nonnull
  private static ISTXExpression _convertTreatAsExpression (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTTREATEXPR);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 1)
      _throwUnexpectedChildrenCount (aNode, "Expected 1 children!");

    // no treat as expression - take only the contained castable expression
    return _convertCastableAsExpression (aNode.jjtGetChild (0));
  }

  // [16] InstanceofExpr ::= TreatExpr
  @Nonnull
  private static ISTXExpression _convertInstanceofExpression (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTINSTANCEOFEXPR);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 1)
      _throwUnexpectedChildrenCount (aNode, "Expected 1 children!");

    return _convertTreatAsExpression (aNode.jjtGetChild (0));
  }

  // [15] IntersectExceptExpr ::= ValueExpr
  @Nonnull
  private static ISTXExpression _convertIntersectExpression (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTINTERSECTEXCEPTEXPR);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 1)
      _throwUnexpectedChildrenCount (aNode, "Expected 1 children!");

    return _convertValueExpression (aNode.jjtGetChild (0));
  }

  // [14] UnionExpr ::= IntersectExceptExpr
  @Nonnull
  private static ISTXExpression _convertUnionExpression (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTUNIONEXPR);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 1)
      _throwUnexpectedChildrenCount (aNode, "Expected 1 children!");

    return _convertIntersectExpression (aNode.jjtGetChild (0));
  }

  // [13] MultiplicativeExpr ::= UnaryExpr ( ("*" | "div" | "idiv" | "mod")
  // UnaryExpr )*
  @Nonnull
  private static ISTXExpression _convertMultiplicativeExpression (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTMULTIPLICATIVEEXPR);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount < 1 || (nChildCount % 2) != 1)
      _throwUnexpectedChildrenCount (aNode, "Expected 1 or an odd number of children!");

    if (nChildCount == 1)
    {
      // no multiplication - take only the contained union expression
      return _convertUnaryExpression (aNode.jjtGetChild (0));
    }

    int nCurIndex = nChildCount - 1;
    ISTXExpression aTemp = null;
    while (nCurIndex >= 0)
    {
      if (aTemp == null)
        aTemp = _convertUnaryExpression (aNode.jjtGetChild (nCurIndex--));

      final ESTXOperator eOperator = ESTXOperator.getFromIDOrThrow (aNode.jjtGetChild (nCurIndex--).getText ());
      final ISTXExpression aLeft = _convertUnaryExpression (aNode.jjtGetChild (nCurIndex--));
      aTemp = new STXBinaryExpression (aLeft, eOperator, aTemp);
    }
    return aTemp;
  }

  // [12] AdditiveExpr ::= MultiplicativeExpr ( ("+" | "-") MultiplicativeExpr
  // )*
  @Nonnull
  private static ISTXExpression _convertAdditiveExpression (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTADDITIVEEXPR);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount < 1 || (nChildCount % 2) != 1)
      _throwUnexpectedChildrenCount (aNode, "Expected 1 or an odd number of children!");

    if (nChildCount == 1)
    {
      // no additive expression - take only the contained multiplicative
      // expression
      return _convertMultiplicativeExpression (aNode.jjtGetChild (0));
    }

    int nCurIndex = nChildCount - 1;
    ISTXExpression aTemp = null;
    while (nCurIndex >= 0)
    {
      if (aTemp == null)
        aTemp = _convertMultiplicativeExpression (aNode.jjtGetChild (nCurIndex--));

      final ESTXOperator eOperator = ESTXOperator.getFromIDOrThrow (aNode.jjtGetChild (nCurIndex--).getText ());
      final ISTXExpression aLeft = _convertMultiplicativeExpression (aNode.jjtGetChild (nCurIndex--));
      aTemp = new STXBinaryExpression (aLeft, eOperator, aTemp);
    }
    return aTemp;
  }

  // [11] RangeExpr ::= AdditiveExpr ( "to" AdditiveExpr )?
  @Nonnull
  private static ISTXExpression _convertRangeExpression (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTRANGEEXPR);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 1 && nChildCount != 2)
      _throwUnexpectedChildrenCount (aNode, "Expected 1 or 2 children!");

    if (nChildCount == 1)
    {
      // no "to" - take only the contained additive expression
      return _convertAdditiveExpression (aNode.jjtGetChild (0));
    }

    final ISTXExpression aLeft = _convertAdditiveExpression (aNode.jjtGetChild (0));
    final ISTXExpression aRight = _convertAdditiveExpression (aNode.jjtGetChild (1));
    final STXBinaryExpression ret = new STXBinaryExpression (aLeft, ESTXOperator.TO, aRight);
    return ret;
  }

  // [22] GeneralComp ::= "=" | "!=" | "<" | "<=" | ">" | ">="
  // [10] ComparisonExpr ::= RangeExpr ( GeneralComp RangeExpr )?
  @Nonnull
  private static ISTXExpression _convertComparisonExpression (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTCOMPARISONEXPR);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 1 && nChildCount != 3)
      _throwUnexpectedChildrenCount (aNode, "Expected 1 or 3 children!");

    if (nChildCount == 1)
    {
      // no comparator - take only the contained Range expression
      return _convertRangeExpression (aNode.jjtGetChild (0));
    }

    final ISTXExpression aLeft = _convertRangeExpression (aNode.jjtGetChild (0));
    final ESTXOperator eOperator = ESTXOperator.getFromIDOrThrow (aNode.jjtGetChild (1).getText ());
    final ISTXExpression aRight = _convertRangeExpression (aNode.jjtGetChild (2));
    final STXBinaryExpression ret = new STXBinaryExpression (aLeft, eOperator, aRight);
    return ret;
  }

  // [9] AndExpr ::= ComparisonExpr ( "and" ComparisonExpr )*
  @Nonnull
  private static ISTXExpression _convertAndExpression (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTANDEXPR);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount < 1)
      _throwUnexpectedChildrenCount (aNode, "Expected at least 1 child!");

    if (nChildCount == 1)
    {
      // no "and" - take only the contained Comparison expression
      return _convertComparisonExpression (aNode.jjtGetChild (0));
    }

    int nCurIndex = nChildCount - 1;
    ISTXExpression aTemp = null;
    while (nCurIndex >= 0)
    {
      final ISTXExpression aExpr = _convertComparisonExpression (aNode.jjtGetChild (nCurIndex));
      if (aTemp == null)
        aTemp = aExpr;
      else
        aTemp = new STXBinaryExpression (aExpr, ESTXOperator.AND, aTemp);
      nCurIndex--;
    }
    return aTemp;
  }

  // [8] OrExpr ::= AndExpr ( "or" AndExpr )*
  @Nonnull
  private static ISTXExpression _convertOrExpression (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTOREXPR);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount < 1)
      _throwUnexpectedChildrenCount (aNode, "Expected at least 1 child!");

    if (nChildCount == 1)
    {
      // no "or" - take only the contained "and" expression
      return _convertAndExpression (aNode.jjtGetChild (0));
    }

    int nCurIndex = nChildCount - 1;
    ISTXExpression aTemp = null;
    while (nCurIndex >= 0)
    {
      final ISTXExpression aExpr = _convertAndExpression (aNode.jjtGetChild (nCurIndex));
      if (aTemp == null)
        aTemp = aExpr;
      else
        aTemp = new STXBinaryExpression (aExpr, ESTXOperator.OR, aTemp);
      nCurIndex--;
    }
    return aTemp;
  }

  // [7] IfExpr ::= "if" "(" Expr ")" "then" ExprSingle "else" ExprSingle
  @Nonnull
  private static STXIfExpression _convertIfExpression (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTIFEXPR);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 3)
      _throwUnexpectedChildrenCount (aNode, "Expected exactly 3 children!");

    final STXExpressionList aTestExprs = _convertExpressionList (aNode.jjtGetChild (0));
    final ISTXExpression aThenExpr = _convertExpressionSingle (aNode.jjtGetChild (1));
    final ISTXExpression aElseExpr = _convertExpressionSingle (aNode.jjtGetChild (2));

    final STXIfExpression ret = new STXIfExpression (aTestExprs, aThenExpr, aElseExpr);
    return ret;
  }

  // [6] QuantifiedExpr ::= ("some" | "every") "$" VarName "in" ExprSingle (","
  // "$" VarName "in" ExprSingle)* "satisfies" ExprSingle
  @Nonnull
  private static STXQuantifiedExpression _convertQuantifiedExpression (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTQUANTIFIEDEXPR);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount < 4)
      _throwUnexpectedChildrenCount (aNode, "Expected at least 4 children!");
    if ((nChildCount % 2) != 0)
      _throwUnexpectedChildrenCount (aNode, "Expected an even number of children!");

    final ESTXQuantifiedExpressionType eType = ESTXQuantifiedExpressionType.getFromIDOrThrow (aNode.jjtGetChild (0)
                                                                                                   .getText ());
    final int nPairs = (nChildCount / 2) - 1;
    final ICommonsList <STXVarNameAndExpression> aClauses = new CommonsArrayList<> ();
    for (int i = 0; i < nPairs; ++i)
    {
      final ParserQName aVarName = _convertVarName (aNode.jjtGetChild (1 + i * 2));
      final ISTXExpression aExpression = _convertExpressionSingle (aNode.jjtGetChild (2 + i * 2));
      aClauses.add (new STXVarNameAndExpression (aVarName, aExpression));
    }

    final ISTXExpression aSatisfyExpr = _convertExpressionSingle (aNode.jjtGetChild (nChildCount - 1));

    final STXQuantifiedExpression ret = new STXQuantifiedExpression (eType, aClauses, aSatisfyExpr);
    return ret;
  }

  // [5] SimpleForClause ::= "for" "$" VarName "in" ExprSingle ("," "$" VarName
  // "in" ExprSingle)*
  @Nonnull
  private static ICommonsList <STXVarNameAndExpression> _convertSimpleForClause (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTSIMPLEFORCLAUSE);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount < 2)
      _throwUnexpectedChildrenCount (aNode, "Expected at least 2 children!");
    if ((nChildCount % 2) != 0)
      _throwUnexpectedChildrenCount (aNode, "Expected an even number of children!");

    final ICommonsList <STXVarNameAndExpression> ret = new CommonsArrayList<> ();
    for (int i = 0; i < nChildCount; i += 2)
    {
      final ParserQName aVarName = _convertVarName (aNode.jjtGetChild (i));
      final ISTXExpression aExpression = _convertExpressionSingle (aNode.jjtGetChild (i + 1));
      ret.add (new STXVarNameAndExpression (aVarName, aExpression));
    }
    return ret;
  }

  // [4] ForExpr ::= SimpleForClause "return" ExprSingle
  @Nonnull
  private static STXForExpression _convertForExpression (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTFOREXPR);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 2)
      _throwUnexpectedChildrenCount (aNode, "Expected exactly 2 children!");

    final ICommonsList <STXVarNameAndExpression> aForClause = _convertSimpleForClause (aNode.jjtGetChild (0));
    final ISTXExpression aReturnExpression = _convertExpressionSingle (aNode.jjtGetChild (1));

    final STXForExpression ret = new STXForExpression (aForClause, aReturnExpression);
    return ret;
  }

  // [3] ExprSingle ::= ForExpr | QuantifiedExpr | IfExpr | OrExpr
  @Nonnull
  private static ISTXExpression _convertExpressionSingle (@Nonnull final STXNode aNode)
  {
    switch (aNode.getNodeType ())
    {
      case ParserSTXTreeConstants.JJTFOREXPR:
        return _convertForExpression (aNode);
      case ParserSTXTreeConstants.JJTQUANTIFIEDEXPR:
        return _convertQuantifiedExpression (aNode);
      case ParserSTXTreeConstants.JJTIFEXPR:
        return _convertIfExpression (aNode);
      case ParserSTXTreeConstants.JJTOREXPR:
        return _convertOrExpression (aNode);
      default:
        throw new STXHandlingException (aNode, "Invalid node type for expression!");
    }
  }

  // [2] Expr ::= ExprSingle ("," ExprSingle)*
  @Nonnull
  private static STXExpressionList _convertExpressionList (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTEXPR);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount == 0)
      _throwUnexpectedChildrenCount (aNode, "Expected at least 1 child!");

    final ICommonsList <ISTXExpression> aExpressions = new CommonsArrayList<> ();
    for (int i = 0; i < nChildCount; ++i)
    {
      final STXNode aChildNode = aNode.jjtGetChild (i);
      aExpressions.add (_convertExpressionSingle (aChildNode));
    }

    final STXExpressionList ret = new STXExpressionList (aExpressions);
    return ret;
  }

  // [1] XPath ::= Expr
  @Nullable
  public static STXPath convertToDomainObject (@Nonnull final STXNode aNode)
  {
    _expectNodeType (aNode, ParserSTXTreeConstants.JJTROOT);
    final int nChildCount = aNode.jjtGetNumChildren ();
    if (nChildCount != 1)
      _throwUnexpectedChildrenCount (aNode, "Expected exactly 1 child!");

    final STXPath ret = new STXPath (_convertExpressionList (aNode.jjtGetChild (0)));
    return ret;
  }
}
