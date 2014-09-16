package svnserver.repository.git.layout;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Prefix based reference mapping.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public final class RefMappingPrefix implements RefMapping {
  @NotNull
  private final String gitPrefix;
  @NotNull
  private final String svnPrefix;

  public RefMappingPrefix(@NotNull String gitPrefix, @NotNull String svnPrefix) {
    this.gitPrefix = gitPrefix;
    this.svnPrefix = svnPrefix;
  }

  @Nullable
  @Override
  public String gitToSvn(@NotNull String gitName) {
    return addSlash(changePrefix(gitPrefix, svnPrefix, gitName));
  }

  @Nullable
  @Override
  public String svnToGit(@NotNull String svnPath) {
    return removeSlash(changePrefix(svnPrefix, gitPrefix, svnPath));
  }

  private static String changePrefix(@NotNull String oldPrefix, @NotNull String newPrefix, @NotNull String name) {
    return name.startsWith(oldPrefix) ? newPrefix + name.substring(oldPrefix.length()) : null;
  }

  private static String addSlash(@Nullable String name) {
    return name == null ? null : name + '/';
  }

  private static String removeSlash(@Nullable String name) {
    if (name == null) {
      return null;
    }
    return name.endsWith("/") ? name.substring(0, name.length() - 1) : name;
  }
}
