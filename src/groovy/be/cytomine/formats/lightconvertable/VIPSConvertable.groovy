package be.cytomine.formats.lightconvertable

import be.cytomine.exception.MiddlewareException
import be.cytomine.formats.Format
import be.cytomine.formats.IConvertableImageFormat
import grails.util.Holders
import utils.FilesUtils
import utils.ProcUtils
import utils.ServerUtils

/**
 * Created by hoyoux on 25.09.15.
 */
abstract class VIPSConvertable extends Format implements IConvertableImageFormat {
    public String[] extensions = null
    public List<String> iipURL = ServerUtils.getServers(Holders.config.cytomine.iipImageServerCyto)

    @Override
    String[] convert() {
        String ext = FilesUtils.getExtensionFromFilename(absoluteFilePath).toLowerCase()
        String source = absoluteFilePath
        File current = new File(absoluteFilePath)
        String target
        if(current.name.lastIndexOf(".") > -1)
            target = current.parent+"/" + current.name.substring(0,current.name.lastIndexOf("."))+"_pyr.tif"
        else
            target = current.parent+"/" + current.name+"_pyr.tif"

        target = target.replace(" ","_")
        println "ext : $ext"
        println "source : $source"
        println "target : $target"

        //1. Look for vips executable
        def vipsExecutable = Holders.config.cytomine.vips

        //2. Pyramid command
        def pyramidCommand = """$vipsExecutable tiffsave "$source" "$target" --tile --pyramid --compression """
        if(Holders.config.cytomine.imageConversionAlgorithm.equals("lzw")) pyramidCommand += "lzw"
        else pyramidCommand += "jpeg -Q 95"
        pyramidCommand += " --tile-width 256 --tile-height 256 --bigtiff"

        boolean success = true

        success &= (ProcUtils.executeOnShell(pyramidCommand) == 0)

        if (!success) {
            throw new MiddlewareException("VIPS Exception");
        }
        return [target]
    }
}
