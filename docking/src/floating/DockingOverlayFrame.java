package floating;

import docking.Dockable;
import docking.DockingRegion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

// displays the overlay highlight of where the panel will be docked
public class DockingOverlayFrame extends JFrame implements MouseMotionListener, MouseListener {
	private static final double REGION_SENSITIVITY = 0.35;

	private Dockable target;
	private Dockable floating;

	public DockingOverlayFrame() {
		setUndecorated(true);

		setBackground(new Color(0, 0, 100, 30));

		setSize(1, 1);

		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void setFloating(Dockable dockable) {
		floating = dockable;
	}

	public void setTarget(Dockable dockable) {
		target = dockable;

		setVisible(target != null);
	}

	public void update(Point screenPos) {
		if (target == null) {
			return;
		}

		JComponent component = (JComponent) target;

		Point framePoint = new Point(screenPos);
		SwingUtilities.convertPointFromScreen(framePoint, component);

		Point point = (component).getLocation();
		Dimension size = component.getSize();

		double horizontalPct = (framePoint.x - point.x) / (double) size.width;
		double verticalPct = (framePoint.y - point.y) / (double) size.height;

		double horizontalEdgeDist = horizontalPct > 0.5 ? 1.0 - horizontalPct : horizontalPct;
		double verticalEdgeDist = verticalPct > 0.5 ? 1.0 - verticalPct : verticalPct;

		if (horizontalEdgeDist < verticalEdgeDist) {
			if (horizontalPct < REGION_SENSITIVITY) {
				size = new Dimension(size.width / 2, size.height);
			}
			else if (horizontalPct > (1.0 - REGION_SENSITIVITY)) {
				point.x += size.width / 2;
				size = new Dimension(size.width / 2, size.height);
			}
		}
		else {
			if (verticalPct < REGION_SENSITIVITY) {
				size = new Dimension(size.width, size.height / 2);
			}
			else if (verticalPct > (1.0 - REGION_SENSITIVITY)) {
				point.y += size.height / 2;
				size = new Dimension(size.width, size.height / 2);
			}
		}

		SwingUtilities.convertPointToScreen(point, component);

		setLocation(point);
		setSize(size);
	}

	public DockingRegion getRegion(Point screenPos) {
		if (target == null) {
			return DockingRegion.CENTER;
		}

		JComponent component = (JComponent) target;

		Point framePoint = new Point(screenPos);
		SwingUtilities.convertPointFromScreen(framePoint, component);

		Point point = (component).getLocation();
		Dimension size = component.getSize();

		double horizontalPct = (framePoint.x - point.x) / (double) size.width;
		double verticalPct = (framePoint.y - point.y) / (double) size.height;

		double horizontalEdgeDist = horizontalPct > 0.5 ? 1.0 - horizontalPct : horizontalPct;
		double verticalEdgeDist = verticalPct > 0.5 ? 1.0 - verticalPct : verticalPct;

		if (horizontalEdgeDist < verticalEdgeDist) {
			if (horizontalPct < REGION_SENSITIVITY) {
				return DockingRegion.WEST;
			}
			else if (horizontalPct > (1.0 - REGION_SENSITIVITY)) {
				return DockingRegion.EAST;
			}
		}
		else {
			if (verticalPct < REGION_SENSITIVITY) {
				return DockingRegion.NORTH;
			}
			else if (verticalPct > (1.0 - REGION_SENSITIVITY)) {
				return DockingRegion.SOUTH;
			}
		}
		return DockingRegion.CENTER;
	}

	// we don't want to use the mouse events in this overlay frame because that would break the app
	// pass them off to the component that we really need them in, the drag source
	private void dispatchEvent(MouseEvent e) {
		if (floating != null) {
			floating.dragSource().dispatchEvent(e);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		dispatchEvent(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		dispatchEvent(e);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		dispatchEvent(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		dispatchEvent(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		dispatchEvent(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		dispatchEvent(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		dispatchEvent(e);
	}
}