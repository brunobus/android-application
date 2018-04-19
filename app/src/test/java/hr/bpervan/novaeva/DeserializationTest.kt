package hr.bpervan.novaeva

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.junit.Assert.*
import org.junit.Test

class JsonDeserializeTest {

    private class TestDTO {
        var id: Long? = null
        var name: String? = null
        var description: String = ""
        var disabled: Boolean = false
        var children: List<String> = emptyList()
    }

    @Test
    fun testMissingValues() {
        val gson: Gson = GsonBuilder().create()

        val json = """{"id": 5, "name": "TestName"}"""

        val skill = gson.fromJson(json, TestDTO::class.java)
        assertEquals(5L, skill.id)
        assertEquals("TestName", skill.name)
        assertEquals("", skill.description)
        assertFalse(skill.disabled)
        assertTrue(skill.children.isEmpty())
    }
}
