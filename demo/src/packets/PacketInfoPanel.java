/*
Copyright (c) 2023 Andrew Auclair

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
package packets;

import ModernDocking.Dockable;
import ModernDocking.DockableStyle;
import ModernDocking.Docking;

import javax.swing.*;

public class PacketInfoPanel extends JPanel implements Dockable {
	public PacketInfoPanel() {
		Docking.registerDockable(this);
	}

	@Override
	public String persistentID() {
		return "packet-info";
	}

	@Override
	public int type() {
		return 0;
	}

	@Override
	public String tabText() {
		return "Info";
	}

	@Override
	public Icon icon() {
		return null;
	}

	@Override
	public boolean floatingAllowed() {
		return false;
	}

	@Override
	public boolean limitToRoot() {
		return true;
	}

	@Override
	public DockableStyle style() {
		return null;
	}

	@Override
	public boolean allowClose() {
		return true;
	}

	@Override
	public boolean allowPinning() {
		return true;
	}

	@Override
	public boolean allowMinMax() {
		return false;
	}

	@Override
	public boolean hasMoreOptions() {
		return false;
	}
}