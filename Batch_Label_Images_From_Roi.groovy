#@ File(label="Image Directory", style="directory") imageDir
#@ File(label="Roi Directory", style="directory") roiDir
#@ File(label="Output Directory", style="directory") outputDir


import java.io.File
import ij.IJ
import ij.io.Opener
import ij.ImagePlus
import groovy.io.FileType
import ij.plugin.frame.RoiManager

/**
 * Creates a new label image with the imp with and height
 * and each Roi is assigned a unique gray value
 * 
 * NOTE: swith to 16-bit if there are more than 255 annotations
 */
ImagePlus roiToLabel (ImagePlus imp, RoiManager rm) {
	impLabel = IJ.createImage("Labeling", "8-bit black", imp.getWidth(), imp.getHeight(), 1)
	ip = impLabel.getProcessor()
	
	rm.getRoisAsArray().eachWithIndex { roi, index ->
		ip.setColor(index+1)
		ip.fill(roi)
	}
	
	ip.resetMinAndMax()
	IJ.run(impLabel, "glasbey inverted", "")
	return impLabel
}

// get list of images in imageDir
def imageList = []
imageDir.eachFile(FileType.FILES) {
    imageList << it.name
}

// get list of roi sets in roiDir
def roiList = []
roiDir.eachFile(FileType.FILES) {
    roiList << it.name
}

// check that both folders contain the same amount of files
assert imageList.size() == roiList.size()

// define Opener and RoiManager
Opener opener = new Opener()
RoiManager rm = new RoiManager(false)

// loop through the images
for (i in 0..imageList.size()-1) {
	// import image
	File imageFile = new File (imageDir, imageList[i])
	ImagePlus imp = opener.openUsingBioFormats((String)imageFile.getAbsolutePath())
	
	// import roi(s)
	File roiFile = new File (roiDir, roiList[i])
	rm.open((String)roiFile.getAbsolutePath())
	
	// create label image
	impLabel = roiToLabel(imp, rm)
	
	// export label image
	String title = imp.getShortTitle()
	String exportPath = new File(outputDir, "${->title}.tif").getAbsolutePath()
	ij.IJ.save(impLabel, exportPath)
	
	// clean up
	imp.close()
	impLabel.close()
	rm.deselect()
	rm.runCommand("Delete")
}