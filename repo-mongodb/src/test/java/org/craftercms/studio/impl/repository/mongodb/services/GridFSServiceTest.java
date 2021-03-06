/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.impl.repository.mongodb.services;

import java.io.InputStream;

import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.craftercms.studio.impl.repository.mongodb.exceptions.MongoRepositoryException;
import org.craftercms.studio.impl.repository.mongodb.services.impl.GridFSServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for GridFS Service.
 */
public class GridFSServiceTest {

    /**
     *  Ruler of the planet Omicron Persei 8
     */
    private static final String FILE_NAME = "Lrrr";
    /**
     * Tested Class.
     */
    private GridFSServiceImpl gridFSService;
    /**
     * GridFsTemplate to be mock.
     */
    private GridFsTemplate gridFsTemplate;

    @Before
    public void setUp() throws Exception {
        gridFSService = new GridFSServiceImpl();
        gridFsTemplate = mock(GridFsTemplate.class);
        gridFSService.setGridFsTemplate(gridFsTemplate);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveFileNameIsNull() throws Exception {
        gridFSService.saveFile(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveFileNameIsEmpty() throws Exception {
        gridFSService.saveFile("", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveFileNameIsBlank() throws Exception {
        gridFSService.saveFile("    ", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveFileInputStreamIsNull() throws Exception {
        gridFSService.saveFile(FILE_NAME, null);
    }

    @Test(expected = MongoRepositoryException.class)
    public void testSaveFailDataAccess() throws Exception {
        when(gridFsTemplate.store((InputStream)Mockito.any(), Mockito.anyString())).thenThrow
            (DataAccessResourceFailureException.class);
        InputStream inputStream = this.getClass().getResourceAsStream("/files/index.xml");
        Assert.assertNotNull("Test Input Stream is null", inputStream); //make sure we read the file.
        gridFSService.saveFile(FILE_NAME, inputStream);
    }

    @Test()
    public void testSave() throws Exception {
        TestGridFsFile mockSavedFile = new TestGridFsFile();
        when(gridFsTemplate.store((InputStream)Mockito.any(), Mockito.anyString())).thenReturn(mockSavedFile);
        InputStream inputStream = this.getClass().getResourceAsStream("/files/index.xml");
        Assert.assertNotNull("Test Input Stream is null", inputStream); //make sure we read the file.
        GridFSFile savedFile = gridFSService.saveFile(FILE_NAME, inputStream);
        Assert.assertEquals(savedFile.getId(), "Omicron Persei 8");//should be the same.
    }

    class TestGridFsFile extends GridFSDBFile {


        @Override
        public Object getId() {
            return "Omicron Persei 8";
        }


    }


}
