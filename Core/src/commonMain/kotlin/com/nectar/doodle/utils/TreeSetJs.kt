package com.nectar.doodle.utils

/**
 * Created by Nicholas Eddy on 4/11/18.
 */
class BstNode<E>(var value: E) {
    var left  = null as BstNode<E>?
    var right = null as BstNode<E>?
}

@Suppress("PrivatePropertyName", "FunctionName")
open class TreeSetJs<E> constructor(private val comparator: Comparator<E>, elements: Collection<E>): Set<E> {
    constructor(comparator: Comparator<E>): this(comparator, emptyList<E>())

    protected var root = null as BstNode<E>?

    init {
        elements.forEach { add(it) }
    }

    override val size get() = size_

    private var size_ = 0

    override fun isEmpty() = root == null

    override fun contains(element: E) = if (isEmpty()) false else contains(root, element)

    override fun containsAll(elements: Collection<E>) = if (isEmpty()) false else elements.all { contains(it) }

    override fun iterator(): Iterator<E> = BstIterator()

    override fun toString(): String {
        return "[${ iterator().asSequence().joinToString(", ")}]"
    }

    protected open fun add(element: E): Boolean {
        return if (root == null) {
            root = BstNode(element)
            ++size_
            true
        } else {
            add(root!!, element).ifTrue { ++size_ }
        }
    }

    protected open fun remove_(element: E): Boolean {
        return (root?.let {
            if (it.value == element) {

                val auxRoot = BstNode(it.value)

                auxRoot.left = root

                val result = remove(it, auxRoot, element)

                root = auxRoot.left

                result

            } else {
                remove(it, null, element)
            }

        } ?: false).ifTrue { --size_; if (size < 0) { throw Exception("BROKEN!!!!") } }
    }

    private fun add(node: BstNode<E>, element: E): Boolean = when {
        node.value == element -> false
        comparator.compare(node.value, element) > 0 -> when (node.left) {
            null -> { node.left = BstNode(element); true }
            else -> add(node.left!!, element)
        }
        else -> when (node.right) {
            null -> { node.right = BstNode(element); true }
            else -> add(node.right!!, element)
        }
    }

    private fun remove(from: BstNode<E>, parent: BstNode<E>?, element: E): Boolean {
        when {
            comparator.compare(element, from.value) < 0 -> return from.left?.let  { remove(it, from, element) } ?: false
            comparator.compare(element, from.value) > 0 -> return from.right?.let { remove(it, from, element) } ?: false
            else                                        -> {
                if (from.left != null && from.right != null) {
                    from.right?.let {
                        from.value = minValue(it)

                        return remove(it, from, from.value)
                    }
                } else if (parent?.left == from) {
                    parent.left = from.left ?: from.right
                    return true

                } else if (parent?.right == from) {
                    parent.right = from.left ?: from.right
                    return true
                }
            }
        }

        return false
    }

    private fun minValue(from: BstNode<E>): E = from.left?.let {
        minValue(it)
    } ?: from.value


    private fun contains(node: BstNode<E>?, element: E): Boolean = when {
        node == null                  -> false
        node.value == element         -> true
        contains(node.left,  element) -> true
        contains(node.right, element) -> true
        else                          -> false
    }

    protected inner class BstIterator: kotlin.collections.MutableIterator<E> {
        private val stack by lazy {
            mutableListOf<BstNode<E>>().also {
                populateStack(root, it)
            }
        }

        override fun remove() {
            if (hasNext()) {
                val node = pop()

                remove_(node.value)
            }
        }

        override fun hasNext() = stack.isNotEmpty()

        override fun next(): E {
            if (!hasNext()) {
                throw NoSuchElementException("The tree has no more elements")
            }

            return pop().value
        }

        private fun pop(): BstNode<E> {
            val node = stack.removeAt(stack.lastIndex)

            populateStack(node.right, stack)

            return node
        }

        private fun populateStack(from: BstNode<E>?, stack: MutableList<BstNode<E>>) {
            var node = from

            while (node != null) {
                stack.add(node)
                node = node.left
            }
        }
    }

    companion object {
        operator fun <T: Comparable<T>> invoke(): TreeSet<T> = TreeSet(Comparator { a, b -> a.compareTo(b) })
        operator fun <T: Comparable<T>> invoke(elements: Collection<T>): TreeSet<T> = TreeSet(Comparator { a, b -> a.compareTo(b) }, elements)
    }
}