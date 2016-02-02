/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class InputStreamContentPayload implements ContentPayload {

  public static interface ProvidesInputStream {
    public InputStream createInputStream() throws IOException;
  }

  private final ProvidesInputStream inputStreamProvider;

  public InputStreamContentPayload(ProvidesInputStream inputStreamProvider) {
    super();
    this.inputStreamProvider = inputStreamProvider;
  }

  @Override
  public InputStream createInputStream() throws IOException {
    return inputStreamProvider.createInputStream();
  }

  @Override
  public void writeToPath(Path path) throws IOException {
    Files.copy(createInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
  }

  @Override
  public URI getURI() throws IOException, UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

}
