import java.util.concurrent.atomic.*

class Solution(private val env: Environment) : Lock<Solution.Node> {
    private val tail = AtomicReference<Node?>(null)

    override fun lock(): Node {
        val my = Node()
        val pred = tail.getAndSet(my)
        if (pred != null) {
            my.locked.value = true
            pred.next.set(my)
            while (my.locked.get()) {
                env.park()
            }
        }
        return my
    }

    override fun unlock(node: Node) {
        if (node.next.get() == null) {
            if (tail.compareAndSet(node, null)) {
                return
            } else {
                while (node.next.get() == null) {
                    // No operations.
                }
            }
        }
        node.next.get()!!.locked.value = false
        env.unpark(node.thread)

    }

    class Node {
        val thread: Thread = Thread.currentThread()
        val locked = AtomicReference<Boolean>(false)
        val next: AtomicReference<Node?> = AtomicReference(null)
    }
}