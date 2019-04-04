package be.cytomine.formats.supported

import be.cytomine.exception.FormatException

/*
 * Copyright (c) 2009-2018. Authors: see NOTICE file.
 *
 * Licensed under the GNU Lesser General Public License, Version 2.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/lgpl-2.1.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import grails.util.Holders
import utils.FilesUtils
import utils.ServerUtils

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

/**
 * Created by stevben on 22/04/14.
 */
class JPEG2000Format extends SupportedImageFormat {

    public JPEG2000Format() {
        extensions = ["jp2"]
        mimeType = "image/jp2"
        iipURL = ServerUtils.getServers(Holders.config.cytomine.iipImageServerJpeg2000)
    }

    public boolean detect() {
        //I check the extension for the moment because did not find an another way
        boolean detect = FilesUtils.getExtensionFromFilename(absoluteFilePath).toLowerCase() == "jp2"
        if(detect && !Holders.config.cytomine.Jpeg2000Enabled) throw new FormatException("JPEG2000 disabled");

        return detect
    }

    @Override
    BufferedImage associated(String label) {
        if (label == "macro")  return null
        return thumb(256)
    }

    public BufferedImage thumb(int maxSize) {
        //construct IIP2K URL
        //maxSize currently ignored because we need to know width of the image with IIP
        String thumbURL = "${ServerUtils.getServer(iipURL)}?fif=$absoluteFilePath&SDS=0,90&CNT=1.0&HEI=$maxSize&WID=$maxSize&CVT=jpeg&QLT=99"
        return ImageIO.read(new URL(thumbURL))
    }

    public def properties() {
        String propertiesURL = "${ServerUtils.getServer(iipURL)}?fif=$absoluteFilePath&obj=IIP,1.0&obj=Max-size&obj=Tile-size&obj=Resolution-number"
        String propertiesTextResponse = new URL(propertiesURL).text
        Integer width = null
        Integer height = null
        propertiesTextResponse.eachLine { line ->
            if (line.isEmpty()) return;

            def args = line.split(":")
            if (args.length != 2) return

            if (args[0].equals('Max-size')) {
                def sizes = args[1].split(' ')
                width = Integer.parseInt(sizes[0])
                height = Integer.parseInt(sizes[1])
            }
        }
        assert(width)
        assert(height)
        def properties = [[key : "mimeType", value : mimeType]]
        properties << [ key : "cytomine.width", value : width ]
        properties << [ key : "cytomine.height", value : height ]
        properties << [ key : "cytomine.resolution", value : null ]
        properties << [ key : "cytomine.magnification", value : null ]
        return properties
    }


}
