package de.tu_dresden.inf.lat.evee.proofs.tools;

import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProgressTracker;

public class BasicProgressBar implements IProgressTracker {
    private String message = "Progress:";
    private int size = 50;
    private long max = 100;
    private long progress = 0;

    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public void setProgress(long progress) {
        this.progress = progress;
        render();
    }

    @Override
    public long getProgress(){
        return progress;
    }

    @Override
    public void setMax(long max) {
        this.max = max;
    }

    @Override
    public void done() {
        if (progress < max) {
            setProgress(max);
        }
        renderDone();
    }

    private void render() {
        int donePct = (int)(100*(((double) progress) / max));
        int doneSize = (int)(size*(((double) progress) / max));
        StringBuilder bar = new StringBuilder("[");

        for (int i = 0; i < size; i++) {
            bar.append(i < doneSize ? "#" : " ");
        }
        bar.append("]");

        System.out.println(message);
        System.out.println(bar + " " + donePct + "%" + "("+progress+"/"+max+")");
    }

    private void renderDone() {
        System.out.println(message + " Done!");
    }
    
}
