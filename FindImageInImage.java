import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.File;
import javax.imageio.ImageIO;

public class FindImageInImage {
    static final int versionMajor = 0;
    static final int versionMinor = 1;

    static final int EXIT_CODE_IMAGE_FOUND = 0;
    static final int EXIT_CODE_IMAGE_NOT_FOUND = 1;
    static final int EXIT_CODE_INVALID_ARGUMENTS = 2;
    static final int EXIT_CODE_CANNOT_READ_IMAGE = 3;
    static final int EXIT_CODE_SUB_IMAGE_WIDTH_TOO_LARGE = 4;
    static final int EXIT_CODE_SUB_IMAGE_HEIGHT_TOO_LARGE = 5;

    public static void main( String[] args ) throws Exception {
        if ( args.length == 0 ) {
            printUsage();
            System.exit( 0 );
        }

        String filename1 = null;
        String filename2 = null;
        int allowedPixelColorDifference = 0;
        double allowedPixelFailsPercent = 0.0;
        int argIndex = 0;
        while ( argIndex < args.length ) {
            String arg = args[ argIndex ];
            if ( arg.equals( "--help" ) ) {
                printUsage();
                System.exit( 0 );
            } else if ( arg.equals( "--version" ) ) {
                printVersion();
                System.exit( 0 );
            } else if ( arg.equals( "--allowed-pixel-color-difference" ) ) {
                if ( argIndex == args.length -1 ) {
                    println( "ERROR: Missing value for parameter: " + arg );
                    System.exit( EXIT_CODE_INVALID_ARGUMENTS );
                }
                try {
                    argIndex++;
                    allowedPixelColorDifference = Integer.parseInt( args[ argIndex ] );
                } catch ( Exception e ) {
                    println( "ERROR: Invalid value for parameter: " + arg );
                    println();
                    printUsage();
                    System.exit( EXIT_CODE_INVALID_ARGUMENTS );
                }
            } else if ( arg.equals( "--allowed-pixel-fails-percent" ) ) {
                if ( argIndex == args.length -1 ) {
                    println( "ERROR: Missing value for parameter: " + arg );
                    System.exit( EXIT_CODE_INVALID_ARGUMENTS );
                }
                try {
                    argIndex++;
                    allowedPixelFailsPercent = Double.parseDouble( args[ argIndex ] );
                } catch ( Exception e ) {
                    println( "ERROR: Invalid value for parameter: " + arg );
                    printUsage();
                    System.exit( EXIT_CODE_INVALID_ARGUMENTS);
                }
            } else if ( filename1 == null ) {
                filename1 = arg;
            } else if ( filename2 == null ) {
                filename2 = arg;
            } else {
                println( "ERROR: Invalid argument: " + arg );
                println();
                printUsage();
                System.exit( EXIT_CODE_INVALID_ARGUMENTS );
            }
            argIndex++;
        }

        BufferedImage subImage = readImage( filename1 );
        BufferedImage img = null;
        if ( filename2 == null ) {
            img = new Robot().createScreenCapture( new Rectangle( Toolkit.getDefaultToolkit().getScreenSize() ) );
        } else {
            img = readImage( filename2 );
        }

        int width = img.getWidth();
        int subWidth = subImage.getWidth();
        int height = img.getHeight();
        int subHeight = subImage.getHeight();

        if ( subWidth > width ) {
            println( "ERROR: Sub image width is larger than image width." );
            System.exit( EXIT_CODE_SUB_IMAGE_WIDTH_TOO_LARGE );
        } else if ( subHeight > height ) {
            println( "ERROR: Sub image height is larger than image height." );
            System.exit( EXIT_CODE_SUB_IMAGE_HEIGHT_TOO_LARGE );
        }

        if ( findSubImage(
                subImage,
                subWidth,
                subHeight,
                img,
                width,
                height,
                allowedPixelFailsPercent,
                allowedPixelColorDifference ) ) {
            System.exit( EXIT_CODE_IMAGE_FOUND );
        }

        System.exit( EXIT_CODE_IMAGE_NOT_FOUND );
    }

    static boolean findSubImage(
            BufferedImage subImage,
            int subWidth,
            int subHeight,
            BufferedImage img,
            int width,
            int height,
            double allowedPixelFailsPercent,
            int allowedPixelColorDifference ) {
        println( "SUB_IMAGE_TOTAL_PIXELS=" + subWidth * subHeight );
        println( "IMAGE_TOTAL_PIXELS=" + width * height );
        int allowedPixelFailsCount = (int)( ( (double)( subWidth * subHeight) / 100.0 ) * allowedPixelFailsPercent );
        println( "ALLOWED_PIXEL_FAILS_COUNT=" + allowedPixelFailsCount );
        long startMillis = System.currentTimeMillis();
        int xOffsetMax = width - subWidth;
        for ( int xOffset = 0; xOffset <= xOffsetMax; xOffset++ ) {
            int yOffsetMax = height - subHeight;
            for ( int yOffset = 0; yOffset <= yOffsetMax; yOffset++ ) {
                if ( subImageIsAtOffset(
                        subImage,
                        img,
                        xOffset,
                        yOffset,
                        allowedPixelFailsCount,
                        allowedPixelColorDifference ) ) {
                    println( "FOUND=YES" );
                    println( "FOUND_AT_X=" + xOffset );
                    println( "FOUND_AT_Y=" + yOffset );
                    println( "RUN_TIME_MILLIS=" + ( System.currentTimeMillis() - startMillis ) );
                    return true;
                }
            }
        }
        println( "FOUND=NO" );
        println( "RUN_TIME_MILLIS=" + ( System.currentTimeMillis() - startMillis ) );
        return false;
    }

    static boolean subImageIsAtOffset(
            BufferedImage subImage,
            BufferedImage img,
            int xOffset,
            int yOffset,
            int allowedPixelFailsCount,
            int allowedPixelColorDifference ) {
        int width = img.getWidth();
        int height = img.getHeight();
        int subWidth = subImage.getWidth();
        int subHeight = subImage.getHeight();
        int actualPixelFailsCount = 0;

        for ( int subImageX = 0; subImageX < subWidth; subImageX++ ) {
            if ( xOffset + subImageX >= width ) {
                println( "Should not occur-1" );
                return false;
            }
            for ( int subImageY = 0; subImageY < subHeight; subImageY++ ) {
                if ( yOffset + subImageY >= height ) {
                    println( "Should not occur-2" );
                    return false;
                }
                int subImagePixel = subImage.getRGB( subImageX, subImageY );
                int r = ( subImagePixel >> 0 ) & 0x000000ff;
                int g = ( subImagePixel >> 1 ) & 0x000000ff;
                int b = ( subImagePixel >> 2 ) & 0x000000ff;
                int v1 = r + g + b;

                int x = xOffset + subImageX;
                int y = yOffset + subImageY;
                int imgPixel = img.getRGB( x, y );
                r = ( imgPixel >> 0 ) & 0x000000ff;
                g = ( imgPixel >> 1 ) & 0x000000ff;
                b = ( imgPixel >> 2 ) & 0x000000ff;
                int v2 = r + g + b;
                int colorDifference = Math.abs( v1 - v2 );
                if ( colorDifference > allowedPixelColorDifference ) {
                    actualPixelFailsCount++;
                    if ( actualPixelFailsCount > allowedPixelFailsCount ) {
                        return false;
                    }
                }
            }
        }
        println( "FAILED_PIXEL_COUNT=" + actualPixelFailsCount );
        double p = actualPixelFailsCount / ( (double)subWidth * (double)subHeight / 100.0 );
        println( "FAILED_PIXEL_PERCENT=" + p );
        return true;
    }

    static void exitWithMessage( String message, boolean printUsage, int exitCode ) {
        println( message );
        if ( printUsage ) {
            printUsage();
        }
        System.exit( exitCode );
    }

    static void printUsage() {
        println( "USAGE:" );
        println();
        println( " FindImageInImage [options] sub_image [image]" );
        println( " FindImageInImage --help" );
        println( " FindImageInImage --version" );
        println();
        println( " Search for image in another image or the screen." );
        println();
        println("PARAMETERS:");
        println();
        println(" sub_image");
        println("   File name of image to search for");
        println();
        println(" image");
        println("   File name of image to search in.");
        println("   Searches on screen if not specified.");
        println();
        println(" --help");
        println("   Output this information");
        println();
        println(" --version");
        println("   Output version number");
        println();
        println("OPTIONS:");
        println();
        println(" --allowed-pixel-color-difference");
        println("   Allowed pixel color difference.");
        println("   An integer value.");
        println();
        println(" --allowed-pixel-fails-percent");
        println("   Allowed percentage of pixel color mismatches.");
        println("   An integer or floating point value.");
        println();
        println( "EXIT CODES:" );
        println();
        println( " 0 : Sub-image has been found" );
        println( " 1 : Sub-image has not been found" );
        println( " 2 : Error in parameters" );
        println( " 3 : Cannot read one of the image files" );
        println( " 4 : Sub-image width larger than width of image" );
        println( " 5 : Sub-image height larger than height of image" );
        println();
    }

    static BufferedImage readImage( String filename ) {
        try {
            return ImageIO.read( new File( filename ) );
        } catch ( Exception e ) {
            println( "ERROR: Cannot read file: " + filename );
            println();
            printUsage();
            System.exit( EXIT_CODE_CANNOT_READ_IMAGE );
        }
        return null;
    }

    static void printVersion() {
        println( "FindImageInImage v" + versionMajor + "." + versionMinor );
    }

    static void println(String message) {
        System.out.println( message );
    }

    static void println() {
        System.out.println();
    }
}

