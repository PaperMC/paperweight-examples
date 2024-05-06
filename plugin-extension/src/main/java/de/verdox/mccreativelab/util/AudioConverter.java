package de.verdox.mccreativelab.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import de.verdox.mccreativelab.util.ffmpeg.FFMPEGDownloader;
import org.bukkit.Bukkit;
import org.codehaus.plexus.util.FileUtils;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.process.ProcessLocator;
import ws.schild.jave.process.ffmpeg.DefaultFFMPEGLocator;

public class AudioConverter {
    // variables

    /**
     * converts any sound file to a .ogg file
     *
     * @param source the source sound/video file.
     * @param target the new .ogg sound file.
     * @return true if successful.
     */
    public static Boolean anyToOgg(File source, File target) {
        final String oggFormat = "ogg";
        final String oggCodec = "libvorbis";
        if (!source.isFile()) {
            System.err.println("input " + source.getName() + " is not a file!");
            return false;
        }
        /*
         * if (!target.isFile()) { System.err.println("output " +target.getName() +
         * " is not a file!"); return false; }//
         */

        try {

            // set the audio attributes (see https://github.com/a-schild/jave2 and
            // https://codereview.stackexchange.com/questions/20084/converting-audio-to-different-file-formats)
            AudioAttributes audioAttr = new AudioAttributes();
            audioAttr.setBitRate(128000);
            audioAttr.setChannels(2);
            audioAttr.setSamplingRate(44100);
            audioAttr.setCodec(oggCodec);

            // set the encoder
            EncodingAttributes encoAttrs = new EncodingAttributes();
            encoAttrs.setOutputFormat(oggFormat);
            encoAttrs.setAudioAttributes(audioAttr);

            // Encode
            ProcessLocator locator = FFMPEGDownloader.getOrCreateFFMPEGEncoder();
            if(locator == null){
                Bukkit.getLogger().warning("Could not create the ffmpeg locator.");
                return false;
            }
            Encoder encoder = new Encoder(locator);
            encoder.encode(new MultimediaObject(source, locator), target, encoAttrs);
            return true;
        } catch (Exception ex) {
            System.err.println("file " + source.getName() + " could not be converted to audio file (.ogg)!");
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * takes a file name and replaces the type with .ogg
     *
     * @param s
     * @return the file as a [name].ogg file
     */
    private static String toOggFileName(String s) {
        return s.substring(0, s.lastIndexOf('.')) + ".ogg";
    }

    private static String getValidName(String s, ArrayList<String> list) {
        String name = s.substring(0, s.lastIndexOf('.')).replace(" ", "_").replace("'", "").toLowerCase();
        String out = name;
        int i = 1;
        while (list.contains(out)) {
            out = name + i;
            i++;
        }

        return out;
    }

    private static String getFileType(File f) {
        String s = f.getName();
        return s.substring(s.lastIndexOf('.') + 1);

    }

}
