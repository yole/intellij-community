package com.siyeh.ig.confusing;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.psi.*;
import com.siyeh.ig.BaseInspection;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.ig.GroupNames;
import com.siyeh.ig.psiutils.WellFormednessUtils;

public class AssignmentToCatchBlockParameterInspection extends ExpressionInspection {

    public String getDisplayName() {
        return "Assignment to catch block parameter";
    }

    public String getGroupDisplayName() {
        return GroupNames.CONFUSING_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location) {
        return "Assignment to catch block parameter #ref #loc ";
    }

    public BaseInspectionVisitor createVisitor(InspectionManager inspectionManager, boolean onTheFly) {
        return new AssignmentToCatchBlockParameterVisitor(this, inspectionManager, onTheFly);
    }

    private static class AssignmentToCatchBlockParameterVisitor extends BaseInspectionVisitor {
        private AssignmentToCatchBlockParameterVisitor(BaseInspection inspection, InspectionManager inspectionManager, boolean isOnTheFly) {
            super(inspection, inspectionManager, isOnTheFly);
        }

        public void visitAssignmentExpression(PsiAssignmentExpression expression) {
            super.visitAssignmentExpression(expression);
            if(!WellFormednessUtils.isWellFormed(expression)){
                return;
            }
            final PsiExpression lhs = expression.getLExpression();
            if (!(lhs instanceof PsiReferenceExpression)) {
                return;
            }
            final PsiReferenceExpression ref = (PsiReferenceExpression) lhs;
            final PsiElement variable = ref.resolve();
            if (!(variable instanceof PsiParameter)) {
                return;
            }
            if (!(((PsiParameter)variable).getDeclarationScope() instanceof PsiCatchSection)) {
                return;
            }
            registerError(expression);
        }
    }

}
