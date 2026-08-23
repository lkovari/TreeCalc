package com.lkovari.mobile.apps.treecalc.ui.components

enum class TreeElbow {
    NONE,
    L,
    T
}

data class TreeGuideSpec(
    val isRoot: Boolean,
    val isLastSibling: Boolean
) {
    val elbow: TreeElbow
        get() = when {
            isRoot -> TreeElbow.NONE
            isLastSibling -> TreeElbow.L
            else -> TreeElbow.T
        }

    val drawElbow: Boolean
        get() = elbow != TreeElbow.NONE

    val verticalThrough: Boolean
        get() = elbow == TreeElbow.T

    val verticalStopsAtCenter: Boolean
        get() = elbow == TreeElbow.L
}

fun treeGuideSpec(depth: Int, isLastSibling: Boolean): TreeGuideSpec {
    return TreeGuideSpec(
        isRoot = depth == 0,
        isLastSibling = isLastSibling
    )
}

fun childAncestorContinues(
    depth: Int,
    ancestorContinues: List<Boolean>,
    isLastSibling: Boolean
): List<Boolean> {
    return if (depth == 0) {
        ancestorContinues
    } else {
        ancestorContinues + !isLastSibling
    }
}
