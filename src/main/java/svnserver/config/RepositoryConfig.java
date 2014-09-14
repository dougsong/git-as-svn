/**
 * This file is part of git-as-svn. It is subject to the license terms
 * in the LICENSE file found in the top-level directory of this distribution
 * and at http://www.gnu.org/licenses/gpl-2.0.html. No part of git-as-svn,
 * including this file, may be copied, modified, propagated, or distributed
 * except according to the terms contained in the LICENSE file.
 */
package svnserver.config;

import org.jetbrains.annotations.NotNull;
import org.tmatesoft.svn.core.SVNException;
import svnserver.repository.VcsRepository;

import javax.xml.bind.annotation.XmlSeeAlso;
import java.io.IOException;

/**
 * Repository configuration.
 *
 * @author a.navrotskiy
 */
@XmlSeeAlso({
    GitRepositoryConfig.class
})
public interface RepositoryConfig {
  @NotNull
  VcsRepository create() throws IOException, SVNException;
}
