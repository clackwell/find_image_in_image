# find_image_in_image
Command line tool to search for an image in another image or the screen; optionally specify pixel and color tolerance ("fuzzy" search).

Intended use is for GUI automation.

## Usage
    C:\Users\myuser> find_image_in_image.bat --help
    USAGE:
    
     FindImageInImage [options] sub_image [image]
     FindImageInImage --help
    
     Search for image in another image or the screen.
    
    PARAMETERS:
    
     sub_image
       File name of image to search for
    
     image
       File name of image to search in.
       Searches on screen if not specified.
    
     --help
       This information
    
    OPTIONS:
    
      --allowed-pixel-color-difference
        Allowed pixel color difference.
        An integer value.
    
      --allowed-pixel-fails-percent
        Allowed percentage of pixel color mismatches.
        An integer or floating point value.
    
    EXIT CODES:
    
     0 : Sub-image has been found
     1 : Sub-image has not been found
     2 : Error in parameters
     3 : Cannot read one of the image files
     4 : Sub-image width larger than width of image
     5 : Sub-image height larger than height of image
