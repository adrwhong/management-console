package org.duracloud.chunk.manifest.xml;

import org.apache.xmlbeans.XmlException;
import org.duracloud.ChunksManifestDocument;
import org.duracloud.ChunksManifestType;
import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.chunk.manifest.ChunksManifestBean;
import org.duracloud.common.error.DuraCloudRuntimeException;

import java.io.IOException;
import java.io.InputStream;


/**
 * This class is a helper utility for binding ChunksManifest objects to a
 * ChunksManifest xml document.
 *
 * @author Andrew Woods
 *         Date: Feb 9, 2010
 */
public class ManifestDocumentBinding {

    /**
     * This method binds a ChunksManifest object to the content of the arg xml.
     *
     * @param xml manifest document to be bound to ChunksManifest object
     * @return ChunksManifest object
     */
    public static ChunksManifest createManifestFrom(InputStream xml) {
        try {
            ChunksManifestDocument doc = ChunksManifestDocument.Factory.parse(
                xml);
            return ManifestElementReader.createManifestFrom(doc);
        } catch (XmlException e) {
            throw new DuraCloudRuntimeException(e);
        } catch (IOException e) {
            throw new DuraCloudRuntimeException(e);
        }
    }

    /**
     * This method serializes the arg ChunksManifest object into an xml document.
     *
     * @param manifest ChunksManifest object to be serialized
     * @return ChunksManifest xml document
     */
    public static String createDocumentFrom(ChunksManifestBean manifest) {
        ChunksManifestDocument doc = ChunksManifestDocument.Factory
            .newInstance();
        if (null != manifest) {
            ChunksManifestType manifestType = ManifestElementWriter.createChunksManifestElementFrom(
                manifest);
            doc.setChunksManifest(manifestType);
        }
        return doc.toString();
    }

}