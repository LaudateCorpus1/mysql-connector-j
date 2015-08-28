/*
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.

  The MySQL Connector/J is licensed under the terms of the GPLv2
  <http://www.gnu.org/licenses/old-licenses/gpl-2.0.html>, like most MySQL Connectors.
  There are special exceptions to the terms and conditions of the GPLv2 as it is applied to
  this software, see the FOSS License Exception
  <http://www.mysql.com/about/legal/licensing/foss-exception.html>.

  This program is free software; you can redistribute it and/or modify it under the terms
  of the GNU General Public License as published by the Free Software Foundation; version 2
  of the License.

  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  See the GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along with this
  program; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth
  Floor, Boston, MA 02110-1301  USA

 */

package testsuite.mysqlx.devapi;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.mysql.cj.api.x.FetchedDocs;
import com.mysql.cj.api.x.Result;
import com.mysql.cj.x.json.JsonDoc;
import com.mysql.cj.x.json.JsonString;

public class CollectionAddTest extends CollectionTest {
    @Before
    @Override
    public void setupCollectionTest() {
        super.setupCollectionTest();
    }

    @After
    @Override
    public void teardownCollectionTest() {
        super.teardownCollectionTest();
    }

    @Test
    public void testBasicAddString() {
        String json = "{'firstName':'Frank', 'middleName':'Lloyd', 'lastName':'Wright'}".replaceAll("'", "\"");
        Result res = this.collection.add(json).execute();
        assertTrue(res.getLastDocumentId().matches("[a-f0-9]{32}"));

        FetchedDocs docs = this.collection.find("firstName like '%Fra%'").execute();
        JsonDoc d = docs.next();
        JsonString val = (JsonString) d.get("lastName");
        assertEquals("Wright", val.getString());
    }

    @Test
    public void testBasicAddDoc() {
        JsonDoc doc = new JsonDoc().add("firstName", new JsonString().setValue("Georgia"));
        doc.add("middleName", new JsonString().setValue("Totto"));
        doc.add("lastName", new JsonString().setValue("O'Keeffe"));
        Result res = this.collection.add(doc).execute();
        assertTrue(res.getLastDocumentId().matches("[a-f0-9]{32}"));

        FetchedDocs docs = this.collection.find("lastName like 'O\\'Kee%'").execute();
        JsonDoc d = docs.next();
        JsonString val = (JsonString) d.get("lastName");
        assertEquals("O'Keeffe", val.getString());
    }

    @Test
    @Ignore("needs implemented")
    public void testBasicAddMap() {
        Map<String, Object> doc = new HashMap<>();
        doc.put("x", 1);
        doc.put("y", "this is y");
        doc.put("z", new BigDecimal("44.22"));
        Result res = this.collection.add(doc).execute();
        assertTrue(res.getLastDocumentId().matches("[a-f0-9]{32}"));

        FetchedDocs docs = this.collection.find("z >= 44.22").execute();
        JsonDoc d = docs.next();
        JsonString val = (JsonString) d.get("y");
        assertEquals("this is y", val.getString());
    }

    @Test
    public void testAddWithAssignedId() {
        String json = "{'_id': 'Id#1', 'name': '<unknown>'}".replaceAll("'", "\"");
        Result res = this.collection.add(json).execute();
        assertNull(res.getLastDocumentId());

        FetchedDocs docs = this.collection.find("_id == 'Id#1'").execute();
        JsonDoc d = docs.next();
        JsonString val = (JsonString) d.get("name");
        assertEquals("<unknown>", val.getString());
    }

    @Test
    public void testChainedAdd() {
        String json = "{'_id': '1'}".replaceAll("'", "\"");
        this.collection.add(json).add(json.replaceAll("1", "2")).execute();

        assertEquals(true, this.collection.find("_id = 1").execute().hasNext());
        assertEquals(true, this.collection.find("_id = 2").execute().hasNext());
        assertEquals(false, this.collection.find("_id = 3").execute().hasNext());
    }

    @Test
    public void testAddLargeDocument() {
        int docSize = 255 * 1024;
        StringBuilder b = new StringBuilder("{\"_id\": \"large_doc\", \"large_field\":\"");
        for (int i = 0; i < docSize; ++i) {
            b.append('.');
        }
        String s = b.append("\"}").toString();
        this.collection.add(s).execute();

        FetchedDocs docs = this.collection.find().execute();
        JsonDoc d = docs.next();
        assertEquals(docSize, ((JsonString) d.get("large_field")).getString().length());
    }
}
