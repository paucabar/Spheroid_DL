import qupath.lib.images.servers.LabeledImageServer

def imageData = getCurrentImageData()

// Define output path (relative to project)
def outputDir = buildFilePath(PROJECT_BASE_DIR, 'export')
mkdirs(outputDir)
def outputDirBinary = buildFilePath(outputDir, 'binary')
def outputDirImages = buildFilePath(outputDir, 'images')
def name = GeneralTools.getNameWithoutExtension(imageData.getServer().getMetadata().getName())
def prefix = 'P01_'
name = prefix + name
mkdirs(outputDirBinary)
mkdirs(outputDirImages)
def pathLabels = buildFilePath(outputDirBinary, name + '.tif')
def pathImages = buildFilePath(outputDirImages, name + '.tif')

// Define how much to downsample during export (may be required for large images)
double downsample = 4

// Create an ImageServer where the pixels are derived from annotations
def binaryServer = new LabeledImageServer.Builder(imageData)
    .backgroundLabel(0, ColorTools.BLACK) // Specify background label (usually 0 or 255)
    .downsample(downsample)    // Choose server resolution; this should match the resolution at which tiles are exported
    .addUnclassifiedLabel(255)
    .build()


// Create an ImageServer where raw images are downsampled
def server = imageData.getServer()
def region = RegionRequest.createInstance(server, downsample)


// Write the images
writeImage(binaryServer, pathLabels)
writeImageRegion(server, region, pathImages)