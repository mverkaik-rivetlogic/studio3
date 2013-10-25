/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.test.integration;

import org.craftercms.studio.testing.integration.AbstractIntegrationTest;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HomepageIntegrationTest extends AbstractIntegrationTest {

    public HomepageIntegrationTest() throws Exception {
    }

    @Test
    @Ignore
    public void testISaidHello(){
        driver.get(baseUrl + "/");
        final String htmlText = driver.findElement(By.tagName("h2")).getText();
        assertNotNull(driver.findElement(By.tagName("h2")));
        assertEquals(htmlText,"Hello World!");
    }
}
