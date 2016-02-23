import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class FindImageInImage extends Thread {
    static final int versionMajor = 1;
    static final int versionMinor = 0;

    static final int EXIT_CODE_IMAGE_FOUND = 0;
    static final int EXIT_CODE_IMAGE_NOT_FOUND = 1;
    static final int EXIT_CODE_INVALID_ARGUMENTS = 2;
    static final int EXIT_CODE_CANNOT_READ_IMAGE = 3;
    static final int EXIT_CODE_SUB_IMAGE_WIDTH_TOO_LARGE = 4;
    static final int EXIT_CODE_SUB_IMAGE_HEIGHT_TOO_LARGE = 5;

    static String filename1 = null;
    static String filename2 = null;

    static BufferedImage image = null;
    static BufferedImage subImage = null;

    static double allowedPixelFailsPercent;
    static int allowedPixelColorDifference;

    static int userDesiredThreadCount = Runtime.getRuntime().availableProcessors();

    private int startAtY;
    private int stopAtYInclusive;
    private boolean subImageFound = false;
    private ArrayList<Point> foundAtPoint = new ArrayList<Point>();
    private ArrayList<Integer> foundFailedPixelCount = new ArrayList<Integer>();
    private ArrayList<Double> foundFailedPixelPercent = new ArrayList<Double>();

    public FindImageInImage(
            int startAtY,
            int stopAtYInclusive,
            double allowedPixelFailsPercent,
            int allowedPixelColorDifference ) {
        super();
        this.startAtY = startAtY;
        this.stopAtYInclusive = stopAtYInclusive;
        this.allowedPixelFailsPercent = allowedPixelFailsPercent;
        this.allowedPixelColorDifference = allowedPixelColorDifference;
    }

    public void run() {
        if ( findSubImage(
                subImage,
                image,
                startAtY,
                stopAtYInclusive,
                allowedPixelFailsPercent,
                allowedPixelColorDifference,
                foundAtPoint,
                foundFailedPixelCount,
                foundFailedPixelPercent ) ) {
            subImageFound = true;
        }
    }

    public boolean subImageFound() {
        return this.subImageFound;
    }

    public ArrayList<Point> getFoundAtPoint() {
        return this.foundAtPoint;
    }

    public ArrayList<Integer> getFoundFailedPixelCount() {
        return this.foundFailedPixelCount;
    }

    public ArrayList<Double> getFoundFailedPixelPercent() {
        return this.foundFailedPixelPercent;
    }

    public static void main( String[] args ) throws Exception {
        if ( args.length == 0 ) {
            printUsage();
            System.exit( 0 );
        }

        handleArgs(args);
        subImage = readImage( filename1 );
        image = null;
        if ( filename2 == null ) {
            image = new Robot().createScreenCapture( new Rectangle( Toolkit.getDefaultToolkit().getScreenSize() ) );
        } else {
            image = readImage( filename2 );
        }

        if ( subImage.getWidth() > image.getWidth() ) {
            println( "ERROR: Sub image width is larger than image width." );
            System.exit( EXIT_CODE_SUB_IMAGE_WIDTH_TOO_LARGE );
        } else if ( subImage.getHeight() > image.getHeight() ) {
            println( "ERROR: Sub image height is larger than image height." );
            System.exit( EXIT_CODE_SUB_IMAGE_HEIGHT_TOO_LARGE );
        }

        println( "SUB_IMAGE_WIDTH=" + subImage.getWidth() );
        println( "SUB_IMAGE_HEIGHT=" + subImage.getHeight() );
        println( "SUB_IMAGE_TOTAL_PIXELS=" + subImage.getWidth() * subImage.getHeight() );
        println( "IMAGE_TOTAL_PIXELS=" + image.getWidth() * image.getHeight() );
        println( "ALLOWED_PIXEL_FAILS_COUNT=" + (int)( ( (double)( subImage.getWidth() * subImage.getHeight() ) / 100.0 ) * allowedPixelFailsPercent ) );

        int heightDiff = image.getHeight() - subImage.getHeight();
        int threadCount = userDesiredThreadCount;
        FindImageInImage[] threads = new FindImageInImage[ threadCount ];
        int heightPerCPU = heightDiff / threadCount;
        // threadCount: 2
        // image height: 100
        // subimage height: 20
        // heightDiff: 80
        // heightPerCPU: 40
        // Thread 1: 1*40 -> 40*(1+1)
        //  40, 80
        // Thread 0: 0, 40-1

        // Start at 1; the first range will be handled in the initial
        // thread
        for ( int i = 0; i < threadCount; i++ ) {
            int startAtY = heightPerCPU * i;
            int stopAtYInclusive = ( heightPerCPU * ( i + 1 ) ) - 1;
            // Make sure to cover up to image.getHeight() in
            // last thread:
            if ( i == threadCount - 1 ) {
                stopAtYInclusive = heightDiff;
            }
            threads[ i ] = new FindImageInImage(
                startAtY,
                stopAtYInclusive,
                allowedPixelFailsPercent,
                allowedPixelColorDifference );
            threads[ i ].setName( "Thread #" + ( i + 1 ) );
        }

        long startMillis = System.currentTimeMillis();

        for ( int i = 0; i < threads.length; i++ ) {
            threads[ i ].start();
            //threads[ i ].join();
        }

        boolean subImageFound = false;
        int occurrence = 1;
        for ( int i = 0; i < threads.length; i++ ) {
            FindImageInImage t = threads[ i ];
            t.join();
            if ( !t.subImageFound() ) {
                continue;
            }
            if ( !subImageFound ) {
                subImageFound = true;
                println( "FOUND=YES" );
            }
            ArrayList<Point> foundAtPoint = t.getFoundAtPoint();
            ArrayList<Integer> foundFailedPixelCount = t.getFoundFailedPixelCount();
            ArrayList<Double> foundFailedPixelPercent = t.getFoundFailedPixelPercent();
            for ( int i2 = 0; i2 < foundAtPoint.size(); i2++ ) {
                Point p = foundAtPoint.get( i2 );
                println( "FOUND_" + ( i2 + 1 ) + "_AT_X=" + (int)p.getX() );
                println( "FOUND_" + ( i2 + 1 ) + "_AT_Y=" + (int)p.getY() );
                println( "FOUND_" + ( i2 + 1 ) + "_FAILED_PIXEL_COUNT=" + foundFailedPixelCount.get( i2 ) );
                println( "FOUND_" + ( i2 + 1 ) + "_FAILED_PIXEL_PERCENT=" + foundFailedPixelPercent.get( i2 ) );
            }
        }

        println( "RUN_TIME_MILLIS=" + ( System.currentTimeMillis() - startMillis ) );
        if ( subImageFound ) {
            System.exit( EXIT_CODE_IMAGE_FOUND );
        } else {
            println( "FOUND=NO" );
            System.exit( EXIT_CODE_IMAGE_NOT_FOUND );
        }
    }

    static boolean findSubImage(
            BufferedImage subImage,
            BufferedImage image,
            int startAtY,
            int stopAtYInclusive,
            double allowedPixelFailsPercent,
            int allowedPixelColorDifference,
            ArrayList<Point> foundAtPoint,
            ArrayList<Integer> foundFailedPixelCount,
            ArrayList<Double> foundFailedPixelPercent ) {

        //println( Thread.currentThread().getName() + ": startAtY: " + startAtY + ", stopAtYInclusive: " + stopAtYInclusive );
        int subWidth = subImage.getWidth();
        int subHeight = subImage.getHeight();
        int width = image.getWidth();
        int height = image.getHeight();
        int allowedPixelFailsCount = (int)( ( (double)( subWidth * subHeight) / 100.0 ) * allowedPixelFailsPercent );
        int xOffsetMax = width - subWidth;
        for ( int xOffset = 0; xOffset <= xOffsetMax; xOffset++ ) {
            for ( int yOffset = startAtY; yOffset <= stopAtYInclusive; yOffset++ ) {
                int[] returnActualPixelFailsCount = new int[ 1 ];
                if ( isSubImageAtOffset(
                        subImage,
                        image,
                        xOffset,
                        yOffset,
                        allowedPixelFailsCount,
                        allowedPixelColorDifference,
                        returnActualPixelFailsCount ) ) {
                    foundAtPoint.add( new Point( xOffset, yOffset ) );
                    foundFailedPixelCount.add( returnActualPixelFailsCount[ 0 ] );
                    double p = returnActualPixelFailsCount[ 0 ] / ( (double)subWidth * (double)subHeight / 100.0 );
                    foundFailedPixelPercent.add( p );
                }
            }
        }
        return foundAtPoint.size() > 0;
    }

    static boolean isSubImageAtOffset(
            BufferedImage subImage,
            BufferedImage image,
            int xOffset,
            int yOffset,
            int allowedPixelFailsCount,
            int allowedPixelColorDifference,
            int[] returnActualPixelFailsCount ) {
        int width = image.getWidth();
        int height = image.getHeight();
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
                int imagePixel = image.getRGB( x, y );
                r = ( imagePixel >> 0 ) & 0x000000ff;
                g = ( imagePixel >> 1 ) & 0x000000ff;
                b = ( imagePixel >> 2 ) & 0x000000ff;
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
        returnActualPixelFailsCount[ 0 ] = actualPixelFailsCount;
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
        println(" --allowed-pixel-color-difference=x");
        println("   Allowed pixel color difference.");
        println("   An integer value.");
        println();
        println(" --allowed-pixel-fails-percent=x");
        println("   Allowed percentage of pixel color mismatches.");
        println("   An integer or floating point value.");
        println();
        println(" --thread-count=x");
        println("   Number of threads to use.");
        println("   An integer value.");
        println("   Defaults to number of processors reported by JRE.");
        println("   If specified value is larger than number of");
        println("   processors reported by JRE, default to the latter.");
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

    static void handleArgs(String[] args) {
        int argIndex = 0;
        while ( argIndex < args.length ) {
            String arg = args[ argIndex ];
            if ( arg.equals( "--help" ) ) {
                printUsage();
                System.exit( 0 );
            } else if ( arg.equals( "--version" ) ) {
                printVersion();
                System.exit( 0 );
            } else if ( arg.indexOf( "--allowed-pixel-color-difference=" ) != -1 ) {
                try {
                    allowedPixelColorDifference = Integer.parseInt( arg.split( "=", 2 )[ 1 ] );
                } catch ( Exception e ) {
                    println( "ERROR: Invalid value for parameter: " + arg );
                    println();
                    printUsage();
                    System.exit( EXIT_CODE_INVALID_ARGUMENTS );
                }
            } else if ( arg.indexOf( "--allowed-pixel-fails-percent=" ) != -1 ) {
                try {
                    allowedPixelFailsPercent = Double.parseDouble( arg.split( "=", 2 )[ 1 ] );
                } catch ( Exception e ) {
                    println( "ERROR: Invalid value for parameter: " + arg );
                    printUsage();
                    System.exit( EXIT_CODE_INVALID_ARGUMENTS);
                }
            } else if ( arg.indexOf( "--threads=" ) != -1 ) {
                try {
                    userDesiredThreadCount = Integer.parseInt( arg.split( "=", 2 )[ 1 ] );
                    if ( userDesiredThreadCount <= 0 ) {
                        throw new Exception();
                    }
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

