/* Copyright (c) 2008 Ben Howell
 * This software is licensed under the MIT License
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

package com.digero.lotromusic;

public class Version implements Comparable<Version> {
	public static Version parseVersion(String versionString) {
		if (versionString == null)
			return null;

		String[] parts = versionString.trim().split("\\.");
		int major, minor = 0, revision = 0, build = 0;

		try {
			if (parts.length == 0)
				return null;

			major = Integer.parseInt(parts[0]);
			if (parts.length > 1)
				minor = Integer.parseInt(parts[1]);
			if (parts.length > 2)
				revision = Integer.parseInt(parts[2]);
			if (parts.length > 3)
				build = Integer.parseInt(parts[3]);

			return new Version(major, minor, revision, build);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	public final int major, minor, revision, build;

	public Version(int major, int minor, int revision, int build) {
		this.major = major;
		this.minor = minor;
		this.revision = revision;
		this.build = build;
	}

	@Override
	public int compareTo(Version that) {
		if (that == null)
			return 1;

		if (this.major != that.major)
			return this.major - that.major;

		if (this.minor != that.minor)
			return this.minor - that.minor;

		if (this.revision != that.revision)
			return this.revision - that.revision;

		return this.build - that.build;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}

		Version that = (Version) obj;
		return (this.major == that.major) && (this.minor == that.minor)
				&& (this.revision == that.revision) && (this.build == that.build);
	}

	@Override
	public int hashCode() {
		return (major << 15) ^ (minor << 10) ^ (revision << 5) ^ build;
	}

	@Override
	public String toString() {
		return major + "." + minor + "." + revision + "." + build;
	}
}
