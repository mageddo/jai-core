/*
 * $RCSfile: MlibSubtractFromConstOpImage.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005-02-11 04:56:07 $
 * $State: Exp $
 */
package com.sun.media.jai.mlib;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import javax.media.jai.ImageLayout;
import javax.media.jai.OpImage;
import javax.media.jai.PointOpImage;
import java.util.Map;
import com.sun.media.jai.util.ImageUtil;
// import com.sun.media.jai.test.OpImageTester;
import com.sun.medialib.mlib.*;

/**
 * A mediaLib implementation of "SubtractFromConst" operator.
 *
 */
final class MlibSubtractFromConstOpImage extends PointOpImage {
    private double[] constants;

    /**
     * Constructs an MlibSubtractFromConstOpImage. The image dimensions
     * are copied from the source image.  The tile grid layout, SampleModel,
     * and ColorModel may optionally be specified by an ImageLayout object.
     *
     * @param source    a RenderedImage.
     * @param layout    an ImageLayout optionally containing the tile
     *                  grid layout, SampleModel, and ColorModel, or null.
     */
    public MlibSubtractFromConstOpImage(RenderedImage source,
                                        Map config,
                                        ImageLayout layout,
                                        double[] constants) {
        super(source, layout, config, true);
        this.constants = MlibUtils.initConstants(constants,
                                            getSampleModel().getNumBands());
        // Set flag to permit in-place operation.
        permitInPlaceOperation();
    }

    /**
     * Subtract the pixel values of a rectangle from a given constant.
     * The sources are cobbled.
     *
     * @param sources   an array of sources, guarantee to provide all
     *                  necessary source data for computing the rectangle.
     * @param dest      a tile that contains the rectangle to be computed.
     * @param destRect  the rectangle within this OpImage to be processed.
     */
    protected void computeRect(Raster[] sources,
                               WritableRaster dest,
                               Rectangle destRect) {

        Raster source = sources[0];
        int formatTag = MediaLibAccessor.findCompatibleTag(sources, dest);

	// For PointOpImages, the srcRect and the destRect are the same.
        MediaLibAccessor srcAccessor = new MediaLibAccessor(source, destRect,
							    formatTag);
        MediaLibAccessor dstAccessor = new MediaLibAccessor(dest, destRect,
							    formatTag);

        mediaLibImage[] srcML = srcAccessor.getMediaLibImages();
        mediaLibImage[] dstML = dstAccessor.getMediaLibImages();

        switch (dstAccessor.getDataType()) {
        case DataBuffer.TYPE_BYTE:
        case DataBuffer.TYPE_USHORT:
        case DataBuffer.TYPE_SHORT:
        case DataBuffer.TYPE_INT:
            int[] constInt = new int[constants.length];
            for (int i = 0; i < constants.length; i++) {
                constInt[i] = ImageUtil.clampRoundInt(constants[i]);
            }

            for (int i = 0 ; i < dstML.length; i++) {
                int mlconstants[] = dstAccessor.getIntParameters(i, constInt);
                Image.ConstSub(dstML[i], srcML[i], mlconstants);
            }
            break;

        case DataBuffer.TYPE_FLOAT:
        case DataBuffer.TYPE_DOUBLE:
            for (int i = 0 ; i < dstML.length; i++) {
                double[] mlconstants = dstAccessor.getDoubleParameters(i, constants);
                Image.ConstSub_Fp(dstML[i], srcML[i], mlconstants);
            }
            break;

        default:
            String className = this.getClass().getName();
            throw new RuntimeException(className + JaiI18N.getString("Generic2"));
        }

        if (dstAccessor.isDataCopy()) {
            dstAccessor.clampDataArrays();
            dstAccessor.copyDataToRaster();
        }
    }

//     public static OpImage createTestImage(OpImageTester oit) {
//         double[] consts = {5, 5, 5};
//         return new MlibSubtractFromConstOpImage(oit.getSource(), null,
//                                               new ImageLayout(oit.getSource()),
// 					      consts);
//     }

//     // Calls a method on OpImage that uses introspection, to make this
//     // class, discover it's createTestImage() call, call it and then
//     // benchmark the performance of the created OpImage chain.
//     public static void main (String args[]) {
//       String classname = "com.sun.media.jai.mlib.MlibSubtractFromConstOpImage";
//         OpImageTester.performDiagnostics(classname,args);
//     }
}
