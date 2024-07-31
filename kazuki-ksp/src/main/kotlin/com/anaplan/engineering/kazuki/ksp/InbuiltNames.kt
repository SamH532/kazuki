package com.anaplan.engineering.kazuki.ksp

import com.squareup.kotlinpoet.MemberName

internal object InbuiltNames {
    internal const val corePackage = "com.anaplan.engineering.kazuki.core"
    internal const val coreInternalPackage = "com.anaplan.engineering.kazuki.core.internal"

    internal val mkSet = MemberName(corePackage, "mk_Set")
    internal val mkSet1 = MemberName(corePackage, "mk_Set1")
    internal val asSet = MemberName(corePackage, "as_Set")
    internal val asSet1 = MemberName(corePackage, "as_Set1")
    internal val mkSeq = MemberName(corePackage, "mk_Seq")
    internal val mkSeq1 = MemberName(corePackage, "mk_Seq1")
    internal val forall = MemberName(corePackage, "forall")
}