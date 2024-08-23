package com.anaplan.engineering.kazuki.ksp.type

import com.anaplan.engineering.kazuki.ksp.ProcessingState
import com.google.devtools.ksp.processing.Resolver

class TypeGenerationContext(
    val processingState: ProcessingState,
    val resolver: Resolver
) : ProcessingState by processingState {

}