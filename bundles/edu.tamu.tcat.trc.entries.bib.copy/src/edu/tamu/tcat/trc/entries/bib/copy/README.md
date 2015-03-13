Provides a mechanism to associated a bibliographic entry with one or more digital copies of that item.

The role of the **Digital Copy Search Service** is to provide a unified REST API that allows clients to search across multiple data sources to identify potentially relevant digital copies of a bibliographic item. The search service accepts simple key word queries and also allows for date range and author filters. In general, the search implementation will prioritize results based on the date range and author filters but will not strictly remove non-matching entities since not all data sources provide this information.

The search service returns **Copy Proxy** objects with enough detail to support display and retrieval (an identifier, a title, a provider, and rights information if available). *TODO is there additional info that should be provided? Is there a way to supply optional info?*

A **Digital Copy Provider Service** is responsible for providing access to information about digital copies from a single source (e.g., PDFs stored on a server or volumes in HahtiTrust). A copy provider serves two main roles. First, it implements a search strategy (used by the search service) that returns proxy objects in response to a query. Second, it provides implementation specific representations of a digital copy. *NB: will need to provide authorization support*

The **Digital Copy** instances returned by a copy provider represent detailed information about the source representation of a book. Copies are typically scanned versions of the book, but may include other representations such as full text or audio files.

Within the TRC Catalog framework, copies are opaque objects. They are designed to be interpreted by a client that is tightly coupled to the copy provider that instantiated them. These clients are intended to perform two main roles. One is to extract useful metadata information from the digital copy and providing that information to UI controls or other user support components of the system. For example, the HathiTrust records include MARCXML metadata. A client could use this information to assist editors in entering bibliographic information for corresponding bibliographic records. *TODO need scenario/use case*.

The other role of the client is to generate a book reader (or other interface) to allow users to access the copy. For example, a client designed to work with copies from HathiTrust might display an embedded HT Book Reader while one that works with PDF documents might select between displaying the PDF in the browser's native PDF reader or to use the Internet Archive's Book Reader.

A **Digital Copy Reference** links a digital copy with a corresponding TRC Catalog bibliographic entry. This reference includes editorial commentary for display that can be used to describe why this copy was selected for inclusion in the TRC and to indicate why users might find this particular copy interesting. For example, it might indicate that a copy is in color, scanned from microfilm, has missing pages, etc. The reference also can be used by the system to capture other relevant metadata for display. This metadata may be gathered from the digital copy source or supplied by editors. These references are the primary unit for local storage by the TRC framework.


 