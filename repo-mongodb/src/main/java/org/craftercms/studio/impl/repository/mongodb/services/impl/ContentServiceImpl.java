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

package org.craftercms.studio.impl.repository.mongodb.services.impl;

import java.io.InputStream;
import java.util.List;
import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.RepositoryException;
import org.craftercms.studio.api.content.ContentService;
import org.craftercms.studio.api.content.PathService;
import org.craftercms.studio.commons.dto.Item;
import org.craftercms.studio.commons.dto.ItemId;
import org.craftercms.studio.commons.dto.Tree;
import org.craftercms.studio.commons.exception.InvalidContextException;
import org.craftercms.studio.commons.filter.Filter;
import org.craftercms.studio.impl.repository.mongodb.MongoRepositoryDefaults;
import org.craftercms.studio.impl.repository.mongodb.domain.CoreMetadata;
import org.craftercms.studio.impl.repository.mongodb.domain.Node;
import org.craftercms.studio.impl.repository.mongodb.exceptions.MongoRepositoryException;
import org.craftercms.studio.impl.repository.mongodb.services.NodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mongo DB implementation of ContentService.
 */
public class ContentServiceImpl implements ContentService {

    /**
     * Node Service Impl.
     */
    private NodeService nodeService;
    /**
     * System properties.
     */
    //  private PropertyResolver properties;
    /**
     * Logger.
     */
    private Logger log = LoggerFactory.getLogger(ContentServiceImpl.class);
    /**
     * Path Service.
     */
    private PathService pathService;

    @Override
    public Item create(final String ticket, final String site, final String path, final Item item,
                       final InputStream content) throws RepositoryException {
        //Validates that all inputs are ok
        if (StringUtils.isBlank(ticket)) {
            throw new IllegalArgumentException("Ticket can't be null empty or whitespace");
        }
        if (StringUtils.isBlank(site)) {
            throw new IllegalArgumentException("Site can't be null empty or whitespace");
        }

        if (!pathService.isPathValid(path)) {
            throw new IllegalArgumentException("Given Path " + path + " is not valid");
        }

        if (item == null) {
            throw new IllegalArgumentException("Item can't be null");
        }


        Node parent = checkParentPath(ticket, site, path, item);
        log.debug("Saving File");
        Node newFileNode = nodeService.createFileNode(parent, item.getFileName(), item.getLabel(),
            item.getCreatedBy(), content);
        if (newFileNode != null) {
            log.debug("Folder Was created {} ", newFileNode);
            return nodeToItem(newFileNode, ticket, site);
        } else {
            log.error("Folder node was not created ");
            throw new MongoRepositoryException("Unable to create a folder node, due a unknown reason");
        }

    }

    private Node checkParentPath(final String ticket, final String site, final String path,
                                 final Item item) throws MongoRepositoryException {
        String nodeId = pathService.getItemIdByPath(ticket, site, path);
        Node parent;
        if (nodeId == null) {
            log.debug("Could not found parent path {} ",path);
            parent = null;
        } else {
            log.debug("Node id for path {} is {}",path, nodeId);
            parent = nodeService.getNode(nodeId);
        }

        if (parent == null) {
            log.debug("Folder {} not found ", parent);
            if (true) { // TODO Do this with properties.
                log.debug("{} is set true, creating folder structure for saving {} ({}) file",
                    MongoRepositoryDefaults.REPO_MKDIRS, item.getLabel(), item.getFileName());
                parent = nodeService.createFolderStructure(pathService.fullPathFor(site, path));
            } else {
                throw new IllegalArgumentException("Given path " + path + " for site " + site + " does not exist");
            }
        } else if (!nodeService.isNodeFolder(parent)) {
            throw new IllegalArgumentException("Given parent node (base on path " + path + " ) ends with a file not a" +
                " folder");
        }
        return parent;
    }

    @Override
    public Item create(final String ticket, final String site, final String path,
                       final Item item) throws InvalidContextException, RepositoryException {
        //Validates that all inputs are ok
        if (StringUtils.isBlank(ticket)) {
            throw new IllegalArgumentException("Ticket can't be null empty or whitespace");
        }
        if (StringUtils.isBlank(site)) {
            throw new IllegalArgumentException("Site can't be null empty or whitespace");
        }

        if (!pathService.isPathValid(path)) {
            throw new IllegalArgumentException("Given Path " + path + " is not valid");
        }

        if (item == null) {
            throw new IllegalArgumentException("Item can't be null");
        }

        Node folderNode = checkParentPath(ticket, site, path, item);
        log.debug("Saving folder with item information ", item.getFileName(), item.getLabel());
        Node newFolderNode = nodeService.createFolderNode(folderNode, item.getFileName(), item.getLabel(),
            item.getCreatedBy());
        if (newFolderNode != null) {
            log.debug("Folder Was created {} ", newFolderNode);
            return nodeToItem(newFolderNode, ticket, site);
        } else {
            log.error("Folder node was not created ");
            throw new MongoRepositoryException("Unable to create a folder node, due a unknown reason");
        }
    }

    private Item nodeToItem(final Node newNode, String ticket, String site) throws RepositoryException {
        CoreMetadata core = newNode.getMetadata().getCore();
        Item item = new Item();
        item.setPath(pathService.getPathByItemId(ticket, site, newNode.getId()));
        item.setId(new ItemId(newNode.getId()));
        item.setLastModifiedDate(core.getLastModifiedDate());
        item.setModifiedBy(core.getCreator());
        item.setCreatedBy(core.getCreator());
        item.setCreationDate(core.getCreateDate());
        item.setFolder(nodeService.isNodeFolder(newNode));
        item.setModifiedBy(core.getModifier());
        item.setRepoId(newNode.getId());
        item.setLabel(core.getLabel());
        item.setFileName(core.getNodeName());
        if (nodeService.isNodeFile(newNode)) {
            MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
            item.setMimeType(mimeTypesMap.getContentType(item.getFileName()));
        }
        return item;
    }

    @Override
    public InputStream read(final String ticket, final String contentId) throws RepositoryException,
        InvalidContextException {

        //Validates that all inputs are ok
        if (StringUtils.isBlank(ticket)) {
            throw new IllegalArgumentException("Ticket can't be null empty or whitespace");
        }
        if (StringUtils.isBlank(contentId)) {
            throw new IllegalArgumentException("Content Id can't be null empty or whitespace");
        }

        log.debug("Finding inputstream for content with id " + contentId);

        Node item = nodeService.getNode(contentId);

        if (item == null) {
            return null;
        }
        log.debug("Content found {}", item);
        // we can't read folders
        if (nodeService.isNodeFile(item)) {
            log.debug("Content is a file");
            String fileId = item.getMetadata().getCore().getFileId(); //gets the file id
            // File id can't be null,empty or whitespace
            if (StringUtils.isBlank(fileId)) {
                log.error("Node {} is broken, since file id is not a valid ID", item, fileId);
                throw new MongoRepositoryException("Content with Id " + item.toString() + "Is broken since file Id "
                    + fileId + " is not a valid file id");
            }
            InputStream fileInput = nodeService.getFile(fileId);
            // Content should exist with this id, or something is broken.
            if (fileInput == null) {
                log.error("File with Id {} is not found, node is broken", fileId);
                throw new MongoRepositoryException("File with id is not found, node is broken");
            }
            //Now  finally return it .
            return fileInput;
        } else {
            // can't read folders
            log.debug("Content is a folder");
            throw new InvalidContextException("Content with id " + contentId + " is a folder");
        }

    }

    @Override
    public void update(final String ticket, final Item item, final InputStream content) {

    }

    @Override
    public void delete(final String ticket, final String contentId) {

    }

    @Override
    public Tree<Item> getChildren(final String ticket, final String site, final String contentId, final int depth,
                                  final List<Filter> filters) {
        return null;
    }

    @Override
    public void move(final String ticket, final String site, final String sourceId, final String destinationId,
                     final boolean includeChildren) {

    }

    @Override
    public List<String> getSites(final String ticket) {
        return null;
    }

    private boolean siteExists(final String ticket, final String site) {
        return nodeService.getSiteNode(site) != null;
    }

    public void setNodeServiceImpl(final NodeService nodeService) {
        this.nodeService = nodeService;
    }

    //    public void setPropertyResolver(PropertyResolver propertyResolver) {
    //        this.properties = propertyResolver;
    //    }

    public void setPathServices(PathService pathServices) {
        this.pathService = pathServices;
    }


}
