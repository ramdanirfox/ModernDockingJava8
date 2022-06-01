package docking;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

// Main class for the docking framework
// register and dock/undock dockables here
public class Docking {
	public static Dimension frameBorderSize = new Dimension(0, 0);

	private static final Map<String, Dockable> dockables = new HashMap<>();

	private static final Map<JFrame, RootDockingPanel> rootPanels = new HashMap<>();

	public static void registerDockable(Dockable dockable) {
		// TODO register this dockable in a static map, check if it already exists
		if (dockables.containsKey(dockable.persistentID())) {
			// TODO registration failed
		}
		dockables.put(dockable.persistentID(), dockable);
	}

	// Dockables must be deregistered so it can be properly disposed
	public static void deregisterDockable(Dockable dockable) {
		dockables.remove(dockable.persistentID());
	}

	// package private registration function for DockingPanel
	public static void registerDockingPanel(RootDockingPanel panel, JFrame parent) {
		if (frameBorderSize.height == 0) {
			SwingUtilities.invokeLater(() -> {
				Dimension size = parent.getSize();
				Dimension contentsSize = parent.getContentPane().getSize();
				Insets insets = parent.getContentPane().getInsets();

				frameBorderSize = new Dimension(size.width - contentsSize.width - insets.left, size.height - contentsSize.height - insets.top);

				System.out.println("size: " + size + "\ncontents size: " + contentsSize + "\ninsets: " + insets + "\nframe border size: " + frameBorderSize);
			});
		}

		if (rootPanels.containsKey(parent)) {
			// TODO throw an exception, we only allow one root docking panel per frame
		}
		rootPanels.put(parent, panel);
	}

	static void deregisterDockingPanel(JFrame parent) {
		rootPanels.remove(parent);
	}

	public static JFrame findRootAtScreenPos(Point screenPos) {
		for (JFrame frame : rootPanels.keySet()) {
			Point point = new Point(frame.getLocation());
			Rectangle bounds = new Rectangle(point.x, point.y, point.x + frame.getWidth(), point.y + frame.getHeight());

			if (bounds.contains(screenPos)) {
				return frame;
			}
		}
		return null;
	}

	public static RootDockingPanel rootForFrame(JFrame frame) {
		if (rootPanels.containsKey(frame)) {
			return rootPanels.get(frame);
		}
		return null;
	}

	public static Dockable findDockableAtScreenPos(Point screenPos) {
		JFrame frame = findRootAtScreenPos(screenPos);

		// no frame found at the location, return null
		if (frame == null) {
			return null;
		}

		Point framePoint = new Point(screenPos);
		SwingUtilities.convertPointFromScreen(framePoint, frame);

		Component component = SwingUtilities.getDeepestComponentAt(frame, framePoint.x, framePoint.y);

		// no component found at the position, return null
		if (component == null) {
			return null;
		}

		while (!(component instanceof Dockable) && component.getParent() != null) {
			component = component.getParent();
		}

		// didn't find a Dockable, return null
		if (!(component instanceof Dockable)) {
			return null;
		}
		return (Dockable) component;
	}

	public static void dock(JFrame frame, Dockable dockable) {
		dock(frame, dockable, DockingRegion.CENTER);
	}

	public static void dock(JFrame frame, Dockable dockable, DockingRegion region) {
		// TODO throw exception if this frame doesn't have a root
		RootDockingPanel root = rootPanels.get(frame);

		appendDockable(root, dockable, region);
	}

	public static void undock(Dockable dockable) {
		// find the right panel for this and undock it
		for (JFrame frame : rootPanels.keySet()) {
			RootDockingPanel root = rootPanels.get(frame);

			if (root.undock(dockable)) {
				if (root.getPanel() instanceof DockedSimplePanel) {
					rootPanels.remove(frame);
					frame.dispose();
				}
				else if (root.getPanel() instanceof DockedTabbedPanel) {
					DockedTabbedPanel tabbedPanel = (DockedTabbedPanel) root.getPanel();

					// no longer need tabs, switch back to DockedSimplePanel
					if (tabbedPanel.getPanelCount() == 1) {
						root.setPanel(new DockedSimplePanel(tabbedPanel.getPanel(0)));
					}

				}
				return;
			}
		}
	}

	private static void appendDockable(RootDockingPanel root, Dockable dockable, DockingRegion region) {
		if (root.getPanel() == null) {
			root.setPanel(new DockedSimplePanel(new DockableWrapper(dockable)));
		}
		else if (root.getPanel() instanceof DockedSimplePanel) {
			DockedSimplePanel first = (DockedSimplePanel) root.getPanel();

			if (region == DockingRegion.CENTER) {

				DockedTabbedPanel tabbedPanel = new DockedTabbedPanel();
				tabbedPanel.addPanel(first.getDockable());
				tabbedPanel.addPanel(new DockableWrapper(dockable));

				root.setPanel(tabbedPanel);
			}
			else {
				DockedSplitPanel split = new DockedSplitPanel();

				if (region == DockingRegion.EAST || region == DockingRegion.SOUTH) {
					split.setLeft(first.getDockable());
					split.setRight(new DockableWrapper(dockable));
				}
				else {
					split.setLeft(new DockableWrapper(dockable));
					split.setRight(first.getDockable());
				}

				split.setOrientation((region == DockingRegion.EAST || region == DockingRegion.WEST) ? JSplitPane.HORIZONTAL_SPLIT : JSplitPane.VERTICAL_SPLIT);

				root.setPanel(split);
			}
		}
		else if (root.getPanel() instanceof DockedTabbedPanel) {
			DockedTabbedPanel tabbedPanel = (DockedTabbedPanel) root.getPanel();

			tabbedPanel.addPanel(new DockableWrapper(dockable));
		}
	}
}
