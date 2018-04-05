package org.softwire.training.core;

import org.softwire.training.api.ApiImage;
import org.softwire.training.models.License;

import java.util.Arrays;
import java.util.List;

public class BootstrapImages {
    public static final List<BootstrapImage> ALL = Arrays.asList(
            // https://commons.wikimedia.org/wiki/File:Woodpeckers-Telephone-Cable.jpg
            new BootstrapImage(
                    apiImage("Gabrielle Merk", "Woodpeckers on telephone pole and cables.", License.CC_BY_SA),
                    "image/jpeg",
                    "bootstrap/Woodpeckers-Telephone-Cable.jpg"),
            // https://commons.wikimedia.org/wiki/File:Wireway_from_heaven.jpg
            new BootstrapImage(
                    apiImage("Gabrielle Merk", "The cable car Olympos Teleferik on Tahtali mountain, a view from summit station (Antalya Province, Turkey).", License.CC_BY_SA),
                    "image/jpeg",
                    "bootstrap/Wireway-From-Heaven.jpg"),
            // https://commons.wikimedia.org/wiki/File:2017_12_(Professions)_-_Cortume.jpg
            new BootstrapImage(
                    apiImage("Fbrandao.1963", "Tannery workers in Morocco, in their tough task of dyeing the skins that will turn into beautiful pieces of leather.", License.CC_BY_SA),
                    "image/jpeg",
                    "bootstrap/Cortume.jpg"),
            // https://commons.wikimedia.org/wiki/File:Tokyo_blue_facade_at_Nishifukawa_Bridge_1074.jpg
            new BootstrapImage(
                    apiImage("Reinhold Möller", "Blue facade of a house in Tokyo", License.CC_BY_SA),
                    "image/jpeg",
                    "bootstrap/Tokyo-blue-facade-at-Nishifukawa-Bridge.jpg"),
            // https://commons.wikimedia.org/wiki/File:Gera_Orangerie_Magnolia.jpg
                new BootstrapImage(
                    apiImage("Palauenc05", "Magnolia tree in full blossom, Orangerie, Gera (Germany)", License.CC_BY_SA),
                    "image/jpeg",
                    "bootstrap/Gera-Orangerie-Magnolia.jpg"),
            // https://commons.wikimedia.org/wiki/File:Turkish_tea_glass.jpg
                new BootstrapImage(
                    apiImage("Maasaak", "Traditional tea glass in Turkey", License.CC_BY_SA),
                    "image/jpeg",
                    "bootstrap/Turkish-Tea-Glass.jpg"),
            // https://commons.wikimedia.org/wiki/File:Christmas_Tree_Worm,_Thailand,_Spirobranchus_giganteus.jpg
                new BootstrapImage(
                    apiImage("TimSC", "Christmas Tree Worm, Thailand, Spirobranchus giganteus", License.CC_BY_SA),
                    "image/jpeg",
                    "bootstrap/Christmas-Tree-Worm.jpg"),
            // https://commons.wikimedia.org/wiki/File:Feuerschiff_im_Golf_von_Finnland_in_St._Petersburg._IMG_9266WI.jpg
                new BootstrapImage(
                    apiImage("Kora27", "Feuerschiff im Golf von Finnland in St. Petersburg. Russland.", License.CC_BY_SA),
                    "image/jpeg",
                    "bootstrap/Feuerschiff-Im-Golf-Von-Finnland.jpg"),
            // https://commons.wikimedia.org/wiki/File:Wood_at_Leaplish.jpg
            new BootstrapImage(
                    apiImage("The joy of all things", "Fallen tree in the wood at Leaplish next to Kielder Water in Northumberland, England", License.CC_BY_SA),
                    "image/jpeg",
                    "bootstrap/Wood-At-Leaplish.jpg")
    );

    private static ApiImage apiImage(String author, String name, License license) {
        return new ApiImage(null, null, null, author, name, license, null);
    }
}
