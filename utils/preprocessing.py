from skimage.morphology import label
from skimage.segmentation import expand_labels
from skimage.measure import regionprops
import numpy as np
from scipy import ndimage

# Methods to erode label image

# Converts a single label into a binary mask and erodes it
def erode_mask(image, label_id, distance):   
    binary_label_id = np.where(image == label_id, 1, 0) # crates binary image containing only the specified label
    eroded = ndimage.binary_erosion(binary_label_id, iterations = distance) # erodes the binary mask
    eroded_label_id = np.where(eroded == 1, label_id, 0) # creates label image containing only the specified label after erosion

    return eroded_label_id

# Erodes all the masks contained in a label image
def erode_labels(image, distance):
    eroded_labels_list = [] # creates empty list to save images of the eroded labels
    regions = regionprops(image)

    # erodes every label and stores them as individual images
    for i in range(len(regions)):
        label_id = regions[i].label # gets the label id of the current region
        eroded_label = erode_mask(image, label_id, distance) # creates label image containing only the specified label after erosion
        eroded_labels_list.append(eroded_label) # stores the image on the list

    # reconstructs the label image, now containing the eroded labels
    eroded_labels_stack = np.stack(eroded_labels_list) # creates stack from list of images (numpy arrays)
    image_eroded = np.max(eroded_labels_stack, axis = 0) # calculates the maximum projection to get back a 2D, labelled image

    # reassigns labels if any mask is missing after erosion and rises a flag
    if(len(regionprops(image_eroded)) < len(regions)):
        image_eroded = label(image_eroded)
        print('\033[93m[WARNING]: Objects missing after erosion. Consider reducing iterations (distance)')

    return image_eroded

# Method to get semantic labels of objects and edges

# NOTE: distance (in pixels) sets the number of iterations for both eroding and dilating the original labels
# For example, setting a distance of 1 will generate edge labels that are 2 pixels thik (1 eroded + 1 dilated),
# whereas a distance of 2 returns edge labels 4 pixels thick (2 eroded + 2 dilated)

def semantic_edges (image, distance):
    # creates a dilated and an eroded image keeping the label IDs
    expanded_labels = expand_labels(label_image=image, distance=distance) # dilates labels into background regions, but never into neighbouring labels
    eroded_labels = erode_labels(image, distance)

    # creates label images of both edges and objects
    edges = expanded_labels - eroded_labels
    objects = expanded_labels - edges

    # creates semantic labels of both edges and objects
    edges_semantic = np.where(edges > 0, 1, 0) # crates image containing only edge regions (label 1)
    object_semantic = np.where(objects > 0, 2, 0) # crates image containing only object regions (label 2)

    # create stack
    channel_list = [edges_semantic, object_semantic]
    semantic_stack = np.stack(channel_list)  # creates stack from list of images (numpy arrays)
    
    # calculates the maximum projection to get back a 2D, labelled image
    semantic_labels = np.max(semantic_stack, axis = 0)

    return semantic_labels.astype('uint8')