package com.github.dellixou.delclientv3.utils.misc;


import java.awt.*;

/**
 * Raccourcis pour afficher des boîtes de dialogue
 * @author  Bubu
 */
public class Dialogue {

    /**
     * Method warning.
     * Crée une boîte de warning.
     * @param comp, le parent du composant où il faut afficher le dialog.
     * @param msg, le message à afficher.
     */
    public static void warning(Component comp, String msg) {
        warning(comp, msg, "Avertissement");
    }

    /**
     * Method warning.
     * Crée une boîte de warning.
     * @param comp, le parent du composant où il faut afficher le dialog.
     * @param msg, le message à afficher.
     * @param title, le titre du dialog.
     */
    public static void warning(
            Component comp,
            String msg,
            String title) {
        javax.swing.JOptionPane.showMessageDialog(
                comp,
                msg,
                title,
                javax.swing.JOptionPane.WARNING_MESSAGE);
    }


    /**
     * Method error.
     * Crée une boîte d'erreur.
     * @param comp, le parent du composant où il faut afficher le dialog.
     * @param msg, le message à afficher.
     */
    public static void error(Component comp, String msg) {
        error(comp, msg, "Erreur");
    }

    /**
     * Method error.
     * Crée une boîte d'erreur.
     * @param comp, le parent du composant où il faut afficher le dialog.
     * @param msg, le message à afficher.
     * @param title, le titre du dialog.
     */
    public static void error(
            Component comp,
            String msg,
            String title) {
        javax.swing.JOptionPane.showMessageDialog(
                comp,
                msg,
                title,
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Method message.
     * Crée une boîte d'information.
     * @param comp, le parent du composant où il faut afficher le dialog.
     * @param msg, le message à afficher.
     */
    public static void message(Component comp, String msg) {
        message(comp, msg, "Information");
    }

    /**
     * Method messageDialog.
     * Crée une boîte d'information.
     * @param comp, le parent du composant où il faut afficher le dialog.
     * @param msg, le message à afficher.
     * @param title, le titre du dialog.
     */
    public static void message(
            Component comp,
            String msg,
            String title) {
        javax.swing.JOptionPane.showMessageDialog(
                comp,
                msg,
                title,
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Method yesNo.
     * Crée une boite de dialog avec un message et 2 choix:
     * "YES" et "NO".
     * @param comp, le parent du composant où il faut afficher le dialog.
     * @param msg, le message à afficher.
     * @param title, le titre du dialog.
     * @return vrai si la réponse est "YES".
     */
    public static boolean yesNo(
            Component comp,
            String msg,
            String title) {
        return javax.swing.JOptionPane.showConfirmDialog(
                comp,
                msg,
                title,
                javax.swing.JOptionPane.YES_NO_OPTION)==javax.swing.JOptionPane.YES_OPTION;
    }

    /**
     * Method okCancel.
     * Crée une boite de dialog avec un message et 2 choix:
     * "OK" et "Cancel".
     * @param comp, le parent du composant où il faut afficher le dialog.
     * @param msg, le message à afficher.
     * @param title, le titre du dialog.
     * @return vrai si la réponse est "OK".
     */
    public static boolean okCancel(
            Component comp,
            String msg,
            String title) {
        return javax.swing.JOptionPane.showConfirmDialog(
                comp,
                msg,
                title,
                javax.swing.JOptionPane.OK_CANCEL_OPTION)==javax.swing.JOptionPane.OK_OPTION;
    }

    /**
     * Method input.
     * Crée une boite de dialog qui demande à l'utilisateur d'entrer une valeur:
     * @param comp, le parent du composant où il faut afficher le dialog.
     * @param msg, le message à afficher.
     * @param title, le titre du dialog.
     * @param initialValue, la valeur par défaut
     */
    public static Object input(
            Component comp,
            String msg,
            String title,
            String initialValue) {
        if (initialValue==null)
            initialValue="";
        return javax.swing.JOptionPane.showInputDialog(
                comp,
                msg,
                title,
                javax.swing.JOptionPane.QUESTION_MESSAGE,
                null,null,
                initialValue);
    }
}