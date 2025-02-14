package ink.ptms.chemdah.core.conversation

/**
 * Chemdah
 * ink.ptms.chemdah.core.NpcId
 *
 * @author sky
 * @since 2021/2/9 6:19 下午
 */
data class Trigger(val id: List<Id> = emptyList()) {

    data class Id(val namespace: String, val value: String) {

        fun isNPC(namespace: String, value: String): Boolean {
            return namespace.equals(this.namespace, true) && value.equals(this.value, true)
        }
    }
}