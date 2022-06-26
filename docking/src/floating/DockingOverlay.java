/*
Copyright (c) 2022 Andrew Auclair

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package floating;

import docking.Dockable;
import docking.DockingColors;
import docking.DockingRegion;
import docking.RootDockingPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// displays the overlay highlight of where the panel will be docked
public class DockingOverlay implements MouseMotionListener, MouseListener, ComponentListener {
	// determines how close to the edge the user has to drag the panel before they see an overlay other than CENTER
	private static final double REGION_SENSITIVITY = 0.35;

	// the target root for this overlay, always the same
	private final RootDockingPanel targetRoot;

	// the dockable that is currently floating in its own undecoarted frame
	private Dockable floating;

	// the target dockable that the mouse is currently over, could be null
	private Dockable targetDockable;

	// the region on the dockable that is being docked to, this comes from the handles? I think
	private DockingRegion dockableRegion;

	// the region on the root that is being docked to, this comes from the handles? I think
	private DockingRegion rootRegion;

	// the top left location where the overlay starts
	private Point location = new Point(0, 0);
	// the total size of the overlay, used for drawing
	private Dimension size;

	// the utility frame that this overlay belongs to
	private final JFrame utilFrame;

	// whether to draw this overlay, different from swing visiblity because we're manually painting
	private boolean visible = false;

	public DockingOverlay(JFrame utilFrame, RootDockingPanel root) {
		this.utilFrame = utilFrame;

		targetRoot = root;
		size = utilFrame.getSize();

		utilFrame.addMouseListener(this);
		utilFrame.addMouseMotionListener(this);

		root.addComponentListener(this);
	}

	public void setActive(boolean active) {
		visible = active;
	}

	public void setFloating(Dockable dockable) {
		floating = dockable;
	}

	public void setTargetDockable(Dockable dockable) {
		targetDockable = dockable;
	}

	// check if the floating dockable is allowed to dock to this region
	private boolean isRegionAllowed(DockingRegion region) {
		return !floating.disallowedRegions().contains(region);
	}

	public void update(Point screenPos) {
		if (targetRoot != null && rootRegion != null) {
			Point point = targetRoot.getLocation();
			Dimension size = targetRoot.getSize();

			point = SwingUtilities.convertPoint(targetRoot.getParent(), point, utilFrame);

			switch (rootRegion) {
				case WEST -> size = new Dimension(size.width / 2, size.height);
				case NORTH -> size = new Dimension(size.width, size.height / 2);
				case EAST -> {
					point.x += size.width / 2;
					size = new Dimension(size.width / 2, size.height);
				}
				case SOUTH -> {
					point.y += size.height / 2;
					size = new Dimension(size.width, size.height / 2);
				}
			}

			this.location = point;
			this.size = size;
		}
		else if (targetDockable != null && dockableRegion != null) {
			JComponent component = (JComponent) targetDockable;

			Point point = component.getLocation();
			Dimension size = component.getSize();

			point = SwingUtilities.convertPoint(component.getParent(), point, utilFrame);

			switch (dockableRegion) {
				case WEST -> size = new Dimension(size.width / 2, size.height);
				case NORTH -> size = new Dimension(size.width, size.height / 2);
				case EAST -> {
					point.x += size.width / 2;
					size = new Dimension(size.width / 2, size.height);
				}
				case SOUTH -> {
					point.y += size.height / 2;
					size = new Dimension(size.width, size.height / 2);
				}
			}

			this.location = point;
			this.size = size;
		}
		else if (targetDockable != null) {
			JComponent component = (JComponent) targetDockable;

			Point framePoint = new Point(screenPos);
			SwingUtilities.convertPointFromScreen(framePoint, utilFrame);

			Point point = (component).getLocation();
			Dimension size = component.getSize();

			point = SwingUtilities.convertPoint(component.getParent(), point, utilFrame);

			double horizontalPct = (framePoint.x - point.x) / (double) size.width;
			double verticalPct = (framePoint.y - point.y) / (double) size.height;

			double horizontalEdgeDist = horizontalPct > 0.5 ? 1.0 - horizontalPct : horizontalPct;
			double verticalEdgeDist = verticalPct > 0.5 ? 1.0 - verticalPct : verticalPct;

			boolean westAllowed = isRegionAllowed(DockingRegion.WEST);
			boolean eastAllowed = isRegionAllowed(DockingRegion.EAST);

			if (horizontalEdgeDist < verticalEdgeDist && (westAllowed || eastAllowed)) {
				if (horizontalPct < REGION_SENSITIVITY && westAllowed) {
					size = new Dimension(size.width / 2, size.height);
				}
				else if (horizontalPct > (1.0 - REGION_SENSITIVITY) && eastAllowed) {
					point.x += size.width / 2;
					size = new Dimension(size.width / 2, size.height);
				}
			}
			else {
				if (verticalPct < REGION_SENSITIVITY && isRegionAllowed(DockingRegion.NORTH)) {
					size = new Dimension(size.width, size.height / 2);
				}
				else if (verticalPct > (1.0 - REGION_SENSITIVITY) && isRegionAllowed(DockingRegion.SOUTH)) {
					point.y += size.height / 2;
					size = new Dimension(size.width, size.height / 2);
				}
			}

			this.location = point;
			this.size = size;
		}
		else if (targetRoot != null) {
			JComponent component = targetRoot;

			Point point = (component).getLocation();
			Dimension size = component.getSize();

			point = SwingUtilities.convertPoint(component.getParent(), point, utilFrame);

			this.location = point;
			this.size = size;
		}

		utilFrame.revalidate();
		utilFrame.repaint();
	}

	public DockingRegion getRegion(Point screenPos) {
		if (rootRegion != null) {
			return rootRegion;
		}

		if (dockableRegion != null) {
			return dockableRegion;
		}

		// force the region to always be the center if the root is empty
		if (targetRoot.getPanel() == null) {
			return DockingRegion.CENTER;
		}

		JComponent component = targetDockable != null ? (JComponent) targetDockable : targetRoot;

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
		if (floating != null && floating.dragSource() != null) {
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

	public boolean isDockingToRoot() {
		// force the region to always be the center if the root is empty
		if (targetRoot.getPanel() == null) {
			return true;
		}
		return rootRegion != null;
	}

	public boolean isDockingToDockable() {
		return dockableRegion != null || targetDockable != null;
	}

	public void setTargetRootRegion(DockingRegion region) {
		rootRegion = region;
	}

	public void setTargetDockableRegion(DockingRegion region) {
		dockableRegion = region;
	}

	@Override
	public void componentResized(ComponentEvent e) {
		size = utilFrame.getSize();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		location = utilFrame.getLocation();
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	public void paint(Graphics g) {
		if (visible) {
			g.setColor(DockingColors.getDockingOverlay());
			g.fillRect(location.x, location.y, size.width, size.height);
			g.setColor(DockingColors.getDockingOverlayBorder());
			g.fillRect(location.x, location.y, size.width, size.height);
		}
	}
}