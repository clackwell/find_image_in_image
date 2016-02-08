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
        int argIndex = 0;
        while ( argIndex < args.length ) {
            String arg = args[ argIndex ];
            if ( arg.equals( "--help" ) ) {
                printUsage();
                System.exit( 0 );
            } else if ( filename1 == null ) {
                filename1 = arg;
            } else if ( filename2 == null ) {
                filename2 = arg;
            } else {
                println( "ERROR: Invalid argument: " + arg );
                printUsage();
                System.exit( EXIT_CODE_INVALID_ARGUMENTS );
            }
            argIndex++;
        }

        BufferedImage subImage = null;
        try {
            subImage = ImageIO.read( new File( filename1 ) );
        } catch ( Exception e ) {
            println( "ERROR: Cannot read file: " + filename1 );
            printUsage();
            System.exit( EXIT_CODE_CANNOT_READ_IMAGE );
        }

        BufferedImage img = null;
        if ( filename2 == null ) {
            img = new Robot().createScreenCapture( new Rectangle( Toolkit.getDefaultToolkit().getScreenSize() ) );
        } else {
            try {
                img = ImageIO.read( new File( filename2 ) );
            } catch ( Exception e ) {
                println( "ERROR: Cannot read file: " + filename2 );
                printUsage();
                System.exit( EXIT_CODE_CANNOT_READ_IMAGE );
            }
        }

        int width = img.getWidth();
        int subWidth = subImage.getWidth();
        if ( subWidth > width ) {
            println( "ERROR: Sub image width is larger than image width." );
            System.exit( EXIT_CODE_SUB_IMAGE_WIDTH_TOO_LARGE );
        }

        int height = img.getHeight();
        int subHeight = subImage.getHeight();
        if ( subHeight > height ) {
            println( "ERROR: Sub image height is larger than image height." );
            System.exit( EXIT_CODE_SUB_IMAGE_HEIGHT_TOO_LARGE );
        }

        if ( findSubImage( subImage, subWidth, subHeight, img, width, height ) ) {
            System.exit( EXIT_CODE_IMAGE_FOUND );
        }

        System.exit( EXIT_CODE_IMAGE_NOT_FOUND );
    }

    static boolean findSubImage( BufferedImage subImage, int subWidth, int subHeight, BufferedImage img, int width, int height ) {
        long startMillis = System.currentTimeMillis();
        int xOffsetMax = width - subWidth;
        for ( int xOffset = 0; xOffset <= xOffsetMax; xOffset++ ) {
            int yOffsetMax = height - subHeight;
            for ( int yOffset = 0; yOffset <= yOffsetMax; yOffset++ ) {
                if ( subImageIsAtOffset( subImage, img, xOffset, yOffset ) ) {
                    println( "FOUND=YES" );
                    println( "FOUND_AT_X=" + xOffset );
                    println( "FOUND_AT_Y=" + yOffset );
                    println( "RUN_TIME=" + ( System.currentTimeMillis() - startMillis ) );
                    println();
                    return true;
                }
            }
        }
        println( "FOUND=NO" );
        println( "RUN_TIME=" + ( System.currentTimeMillis() - startMillis ) );
        return false;
    }

    static boolean subImageIsAtOffset( BufferedImage subImage, BufferedImage img, int xOffset, int yOffset ) {
        int width = img.getWidth();
        int height = img.getHeight();
        int subWidth = subImage.getWidth();
        int subHeight = subImage.getHeight();

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
                int x = xOffset + subImageX;
                int y = yOffset + subImageY;
                int imgPixel = img.getRGB( x, y );
                if ( subImagePixel != imgPixel ) {
                    return false;
                }
            }
        }
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
        println( " FindImageInImage [--help] [sub_image [image]]" );
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
        println("   This information");
        println();
        println( "EXIT CODES:" );
        println();
        println( " 0 : Sub-image has been found" );
        println( " 1 : Sub-image has not been found" );
        println( " 2 : Error in parameters" );
        println();
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

