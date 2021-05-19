package crux.api.query

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import crux.api.CruxDocument
import crux.api.CruxK
import crux.api.sym
import crux.api.tx.submitTx
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.Duration

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QueryTest {
    companion object {
        private fun createPerson(key: String, forename: String, surname: String) =
            CruxDocument.build(key) {
                it.put("forename", forename)
                it.put("surname", surname)
            }

        private val ivan = createPerson("ivan", "Ivan", "Ivanov")
        private val ivana = createPerson("ivana", "Ivana", "Ivanov")
        private val petr = createPerson("petr", "Petr", "Petrov")

        // The man with no name
        private val clint = CruxDocument.build("clint") {}
    }

    private val db = CruxK.startNode().apply {
        submitTx {
            + ivan
            + ivana
            + petr
            + clint
        }.also {
            awaitTx(it, Duration.ofSeconds(10))
        }
    }.db()

    private fun MutableCollection<MutableList<*>>.singleResults() = map { it[0] }.toSet()
    private fun MutableCollection<MutableList<*>>.simplify() = map { it.toList() }.toSet()

    private val p = "p".sym
    private val p1 = "p1".sym
    private val p2 = "p2".sym
    private val n = "n".sym

    @Nested
    inner class SimpleQueries {

        @Test
        fun `existence of fields`() =
            assertThat(
                db.q {
                    find {
                        + p
                    }

                    where {
                        p has "forename"
                    }
                }.singleResults(),
                equalTo(
                    setOf(
                        "ivan",
                        "ivana",
                        "petr"
                    )
                )
            )

        @Test
        fun `equality of fields`() =
            assertThat(
                db.q {
                    find {
                        + p
                    }

                    where {
                        p has "forename" eq "Ivan"
                    }
                }.singleResults(),
                equalTo(
                    setOf("ivan")
                )
            )

        @Test
        fun `returning more than one value`() =
            assertThat(
                db.q {
                    find {
                        + p
                        + n
                    }

                    where {
                        p has "forename" eq n
                    }
                }.simplify(),
                equalTo(
                    setOf(
                        listOf("ivan", "Ivan"),
                        listOf("ivana", "Ivana"),
                        listOf("petr", "Petr")
                    )
                )
            )
    }
}