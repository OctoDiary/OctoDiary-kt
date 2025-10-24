# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keepclassmembers class org.bxkr.octodiary.DataService {
    public *;
}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep annotation default values (e.g., retrofit2.http.Field.encoded).
-keepattributes AnnotationDefault

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Keep inherited services.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

##---------------End: proguard configuration for Gson  ----------

-keep class org.bxkr.octodiary.models.** { *; }

# PDFBox-Android rules to prevent R8 from removing required classes
-dontwarn java.awt.Graphics2D
-dontwarn java.awt.Graphics
-dontwarn java.awt.Image
-dontwarn java.awt.Point
-dontwarn java.awt.Rectangle
-dontwarn java.awt.RenderingHints
-dontwarn java.awt.Shape
-dontwarn java.awt.color.CMMException
-dontwarn java.awt.color.ColorSpace
-dontwarn java.awt.geom.AffineTransform
-dontwarn java.awt.geom.GeneralPath
-dontwarn java.awt.geom.Path2D$Double
-dontwarn java.awt.geom.Path2D
-dontwarn java.awt.geom.Point2D$Float
-dontwarn java.awt.geom.Point2D
-dontwarn java.awt.image.BufferedImage
-dontwarn java.awt.image.ColorConvertOp
-dontwarn java.awt.image.ColorModel
-dontwarn java.awt.image.DataBuffer
-dontwarn java.awt.image.DataBufferByte
-dontwarn java.awt.image.DataBufferUShort
-dontwarn java.awt.image.ImageObserver
-dontwarn java.awt.image.IndexColorModel
-dontwarn java.awt.image.MultiPixelPackedSampleModel
-dontwarn java.awt.image.Raster
-dontwarn java.awt.image.SampleModel
-dontwarn java.awt.image.WritableRaster
-dontwarn javax.imageio.IIOException
-dontwarn javax.imageio.ImageIO
-dontwarn javax.imageio.ImageReadParam
-dontwarn javax.imageio.ImageReader
-dontwarn javax.imageio.metadata.IIOMetadata
-dontwarn javax.imageio.metadata.IIOMetadataNode
-dontwarn javax.imageio.stream.ImageInputStream
-dontwarn javax.imageio.stream.MemoryCacheImageInputStream
-dontwarn org.apache.fontbox.EncodedFont
-dontwarn org.apache.fontbox.FontBoxFont
-dontwarn org.apache.fontbox.afm.AFMParser
-dontwarn org.apache.fontbox.afm.CharMetric
-dontwarn org.apache.fontbox.afm.FontMetrics
-dontwarn org.apache.fontbox.cff.CFFCIDFont
-dontwarn org.apache.fontbox.cff.CFFCharset
-dontwarn org.apache.fontbox.cff.CFFFont
-dontwarn org.apache.fontbox.cff.CFFParser$ByteSource
-dontwarn org.apache.fontbox.cff.CFFParser
-dontwarn org.apache.fontbox.cff.CFFType1Font
-dontwarn org.apache.fontbox.cff.CIDKeyedType2CharString
-dontwarn org.apache.fontbox.cff.Type2CharString
-dontwarn org.apache.fontbox.cmap.CMap
-dontwarn org.apache.fontbox.cmap.CMapParser
-dontwarn org.apache.fontbox.encoding.Encoding
-dontwarn org.apache.fontbox.ttf.CFFTable
-dontwarn org.apache.fontbox.ttf.CmapLookup
-dontwarn org.apache.fontbox.ttf.CmapSubtable
-dontwarn org.apache.fontbox.ttf.CmapTable
-dontwarn org.apache.fontbox.ttf.HeaderTable
-dontwarn org.apache.fontbox.ttf.NamingTable
-dontwarn org.apache.fontbox.ttf.OS2WindowsMetricsTable
-dontwarn org.apache.fontbox.ttf.OTFParser
-dontwarn org.apache.fontbox.ttf.OpenTypeFont
-dontwarn org.apache.fontbox.ttf.PostScriptTable
-dontwarn org.apache.fontbox.ttf.TTFParser
-dontwarn org.apache.fontbox.ttf.TTFTable
-dontwarn org.apache.fontbox.ttf.TrueTypeCollection$TrueTypeFontProcessor
-dontwarn org.apache.fontbox.ttf.TrueTypeCollection
-dontwarn org.apache.fontbox.ttf.TrueTypeFont
-dontwarn org.apache.fontbox.type1.DamagedFontException
-dontwarn org.apache.fontbox.type1.Type1Font
-dontwarn org.apache.fontbox.util.BoundingBox
-dontwarn org.apache.fontbox.util.autodetect.FontFileFinder

# Keep PDFBox-Android classes
-keep class org.apache.pdfbox.** { *; }
-keep class com.tom_roush.pdfbox.** { *; }
-keep class org.apache.fontbox.** { *; }

# Additional PDFBox and FontBox rules to prevent R8 from removing required classes
-dontwarn java.awt.AlphaComposite
-dontwarn java.awt.BasicStroke
-dontwarn java.awt.Color
-dontwarn java.awt.Composite
-dontwarn java.awt.CompositeContext
-dontwarn java.awt.DisplayMode
-dontwarn java.awt.Font
-dontwarn java.awt.FontMetrics
-dontwarn java.awt.GraphicsConfiguration
-dontwarn java.awt.GraphicsDevice
-dontwarn java.awt.Paint
-dontwarn java.awt.PaintContext
-dontwarn java.awt.RenderingHints$Key
-dontwarn java.awt.Stroke
-dontwarn java.awt.TexturePaint
-dontwarn java.awt.color.ICC_ColorSpace
-dontwarn java.awt.color.ICC_Profile
-dontwarn java.awt.color.ProfileDataException
-dontwarn java.awt.font.FontRenderContext
-dontwarn java.awt.font.GlyphVector
-dontwarn java.awt.geom.Area
-dontwarn java.awt.geom.Ellipse2D$Double
-dontwarn java.awt.geom.NoninvertibleTransformException
-dontwarn java.awt.geom.PathIterator
-dontwarn java.awt.geom.Point2D$Double
-dontwarn java.awt.geom.Rectangle2D$Double
-dontwarn java.awt.geom.Rectangle2D$Float
-dontwarn java.awt.geom.Rectangle2D
-dontwarn java.awt.image.AffineTransformOp
-dontwarn java.awt.image.BufferedImageOp
-dontwarn java.awt.image.ByteLookupTable
-dontwarn java.awt.image.ComponentColorModel
-dontwarn java.awt.image.DataBufferInt
-dontwarn java.awt.image.ImagingOpException
-dontwarn java.awt.image.LookupOp
-dontwarn java.awt.image.LookupTable
-dontwarn java.awt.image.RenderedImage
-dontwarn java.awt.image.renderable.RenderableImage
-dontwarn java.awt.print.Book
-dontwarn java.awt.print.PageFormat
-dontwarn java.awt.print.Paper
-dontwarn java.awt.print.Printable
-dontwarn java.awt.print.PrinterIOException
-dontwarn javax.imageio.IIOImage
-dontwarn javax.imageio.ImageTypeSpecifier
-dontwarn javax.imageio.ImageWriteParam
-dontwarn javax.imageio.ImageWriter
-dontwarn javax.imageio.plugins.jpeg.JPEGImageWriteParam
-dontwarn javax.imageio.stream.ImageOutputStream
-dontwarn javax.imageio.stream.MemoryCacheImageOutputStream
-dontwarn org.apache.fontbox.cff.Type1CharString
-dontwarn org.apache.fontbox.encoding.BuiltInEncoding
-dontwarn org.apache.fontbox.pfb.PfbParser
-dontwarn org.apache.fontbox.ttf.GlyphData
-dontwarn org.apache.fontbox.ttf.GlyphTable
-dontwarn org.apache.fontbox.ttf.HorizontalHeaderTable
-dontwarn org.apache.fontbox.ttf.HorizontalMetricsTable
-dontwarn org.apache.fontbox.ttf.MaximumProfileTable
-dontwarn org.apache.fontbox.ttf.TTFSubsetter
-dontwarn org.apache.fontbox.ttf.VerticalHeaderTable
-dontwarn org.apache.fontbox.ttf.VerticalMetricsTable
-dontwarn com.itextpdf.bouncycastle.BouncyCastleFactory
-dontwarn com.itextpdf.bouncycastlefips.BouncyCastleFipsFactory
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.apache.fontbox.util.Charsets