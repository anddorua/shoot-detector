package anddorua.shootdetector.ui.components;

import com.sun.javafx.binding.StringFormatter;

import javax.swing.*;
import javax.swing.colorchooser.ColorChooserComponentFactory;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by andrey on 05.03.16.
 */
public class BearingGauge extends JComponent {
    public enum HorizDirection {
        LeftPositive, LeftNegative
    }

    public enum VertDirection {
        BaseTop, BaseBottom
    }

    private HorizDirection horizDirection;
    private VertDirection vertDirection;
    private Color scaleColor;
    private Color bearingColor;
    private Color rangeColor;
    private int padding;
    private int bigScaleAngleDeg;
    private int smallScaleAngleDeg;
    private int bigScaleMarkLength;
    private int smallScaleMarkLength;
    private double bearingAngleRad;
    private double bearingRangeMin;
    private double bearingRangeMax;
    private Rectangle innerRect;
    private Stroke bearingStroke;
    private NumberFormat decimalFormat;
    private Font bearingFont = null;

    public BearingGauge() {
        super();
        horizDirection = HorizDirection.LeftNegative;
        vertDirection = VertDirection.BaseBottom;
        scaleColor = Color.BLACK;
        bearingColor = Color.RED;
        rangeColor = Color.YELLOW;
        padding = 4;
        innerRect = new Rectangle();
        bigScaleAngleDeg = 30;
        smallScaleAngleDeg = 5;
        bigScaleMarkLength = 30;
        smallScaleMarkLength = 10;
        this.bearingRangeMin = Math.PI / 6d;
        this.bearingRangeMax = Math.PI / 4d;
        this.bearingAngleRad = (bearingRangeMin + bearingRangeMax) / 2d;
        bearingStroke = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
        decimalFormat = NumberFormat.getNumberInstance();
        decimalFormat.setMaximumFractionDigits(1);
        decimalFormat.setMinimumFractionDigits(1);
        decimalFormat.setMinimumIntegerDigits(1);
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        setInnerDimension();
        AffineTransform savedTransform = g2.getTransform();
        setProperTransform(g2);
        setAntialiasing(g2);
        paintBearing(g);
        paintScale(g);
        g2.setTransform(savedTransform);
        paintValue(g2);
    }

    private void setProperTransform(Graphics2D g2) {
        if (vertDirection == VertDirection.BaseBottom) {
            int height = getHeight();
            g2.translate(0, height);
            g2.scale(1d, -1d);
        }
        if (horizDirection == HorizDirection.LeftPositive) {
            int width = getWidth();
            g2.translate(width, 0);
            g2.scale(-1d, 1d);
        }
    }

    private void paintBearing(Graphics g) {
        if (this.bearingAngleRad == 0 && this.bearingRangeMin == 0 && this.bearingRangeMax == 0) {
            return;
        }
        int centerX = innerRect.x + innerRect.width / 2 + 1;
        int centerY = innerRect.y;
        g.setColor(rangeColor);
        g.fillArc(innerRect.x, innerRect.y - innerRect.height, innerRect.width, innerRect.height * 2, rad2grad(bearingRangeMin) + 270, rad2grad(bearingRangeMax - bearingRangeMin));

        Graphics2D g2 = (Graphics2D)g;
        Stroke old = g2.getStroke();
        g2.setStroke(bearingStroke);
        g.setColor(bearingColor);
        double outerRadius = innerRect.height;
        double paintAngle = bearingAngleRad + Math.PI * 1.5d;
        int dx = (int)Math.round(outerRadius * Math.cos(paintAngle));
        int dy = (int)Math.round(outerRadius * Math.sin(paintAngle));
        g.drawLine(centerX, centerY, centerX + dx, centerY - dy);
        g2.setStroke(old);
    }

    private void setAntialiasing(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }

    private int rad2grad(double angleRad) {
        return (int)Math.round(angleRad / Math.PI * 180);
    }

    public void setBearing(double bearingRad, double bearingRangeMin, double bearingRangeMax) {
        this.bearingAngleRad = bearingRad;
        this.bearingRangeMin = bearingRangeMin;
        this.bearingRangeMax = bearingRangeMax;
        repaint();
    }

    private void paintValue(Graphics2D g2) {
        double valueGrad = bearingAngleRad / Math.PI * 180d;
        String valueToShow = decimalFormat.format(valueGrad);

        if (bearingFont == null) {
            Font currentFont = g2.getFont();
            Map<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>();
            attributes.put(TextAttribute.FAMILY, currentFont.getFamily());
            attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
            attributes.put(TextAttribute.SIZE, (int) (currentFont.getSize() * 2));
            bearingFont = Font.getFont(attributes);
            g2.setFont(bearingFont);
        }

        FontMetrics fm = g2.getFontMetrics();
        int stringWidth = fm.stringWidth(valueToShow);
        int stringHeight = fm.getHeight();
        g2.drawString(valueToShow, innerRect.width / 2 + innerRect.x - stringWidth / 2, innerRect.height / 2 + innerRect.y + stringHeight / 2);
    }

    private void paintScale(Graphics g) {
        g.setColor(scaleColor);
        g.drawRect(innerRect.x, innerRect.y, innerRect.width, innerRect.height);
        int centerX = innerRect.x + innerRect.width / 2 + 1;
        int centerY = innerRect.y;
        g.drawLine(centerX, innerRect.y, centerX, innerRect.y + innerRect.height);
        g.drawArc(innerRect.x, innerRect.y - innerRect.height, innerRect.width, innerRect.height * 2, 0, -180);

        double outerRadius = innerRect.height;
        double innerRadiusBig = outerRadius - bigScaleMarkLength;
        double innerRadiusSmall = outerRadius - smallScaleMarkLength;
        int bigMarkAngle = 0;
        while (bigMarkAngle <= 180) {
            drawScaleMark(centerX, centerY, outerRadius, innerRadiusBig, bigMarkAngle, g);
            int smallMarkAngle = bigMarkAngle + smallScaleAngleDeg;
            bigMarkAngle += bigScaleAngleDeg;
            while (smallMarkAngle < bigMarkAngle && smallMarkAngle < 180) {
                drawScaleMark(centerX, centerY, outerRadius, innerRadiusSmall, smallMarkAngle, g);
                smallMarkAngle += smallScaleAngleDeg;
            }
        }
    }

    void drawScaleMark(int x0, int y0, double outerRadius, double innerRadius, double angleGrad, Graphics g) {
        double angleRad = angleGrad / 180d * Math.PI;
        int x1 = x0 + (int)Math.round(outerRadius * Math.cos(angleRad));
        int y1 = y0 + (int)Math.round(outerRadius * Math.sin(angleRad));
        int x2 = x0 + (int)Math.round(innerRadius * Math.cos(angleRad));
        int y2 = y0 + (int)Math.round(innerRadius * Math.sin(angleRad));
        g.drawLine(x1, y1, x2, y2);
    }


    private void setInnerDimension() {
        getBounds(innerRect);
        int originalWidth = innerRect.width;
        int originalHeight = innerRect.height;
        int innerWidth = innerRect.width - padding * 2;
        if (innerWidth % 2 == 1) {
            innerWidth--;
        }
        int innerHeight = Math.min(innerRect.height - padding * 2, innerWidth / 2);
        innerWidth = Math.min(innerHeight * 2 + 1, innerWidth);
        innerRect.setSize(innerWidth, innerHeight);
        innerRect.setLocation((originalWidth - innerWidth) / 2, (originalHeight - innerHeight) / 2);
    }


}
